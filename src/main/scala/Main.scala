import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.typesafe.config.ConfigFactory
import java.util.zip.GZIPInputStream
import org.joda.time.{DateTime, DateMidnight}
import org.joda.time.DateTimeConstants._
import scalax.io._
import scalaz.Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import util.Try
import java.net.URI
import com.github.theon.uri.Uri.parseUri

object Main extends App {
	implicit val defaultCodec = Codec.UTF8

  lazy val config = ConfigFactory.load()

  lazy val credentials = new AWSCredentials {
    def getAWSAccessKeyId = config.getString("accessKey")
    def getAWSSecretKey = config.getString("secretKey")
  }

  lazy val s3 = new AmazonS3Client(credentials)

  val january = (21 to 31) map (1 -> _)
  val february = (1 to 17) map (2 -> _)
  val days = january ++ february
  val hours = (0 to 23)

  val googleClicks = days.flatMap { case (month, day) =>
    hours.par.flatMap { hour =>
    val resource = Resource.fromInputStream(new GZIPInputStream(
      s3.getObject("ophan-logs", s"2013/$month/$day/$hour.gz").getObjectContent))

    resource.reader.lines().par
      .filter(_.contains("""ref":"http://www.google"""))
      .flatMap { json =>
        (parse(StringInput(json)) \ "client" \ "ref") match {
          case JString(x) =>
            Try {
              val params = parseUri(x).query.params
                for {
                  q <- params.get("q")
                  cd <- params.get("cd")
                } yield Some((q.head.replaceAllLiterally("%20", " ").replaceAllLiterally("+", " "), cd.head) -> 1)
              } getOrElse(None)
        }
      }.map(x => Map(x.get)).reduce(_ |+| _).toList.map(s"2013/$month/${day}T$hour" -> _)
    }
  }

  val output = Resource.fromFile("search-terms.csvish")
  output.writeStrings((googleClicks.toList.sortBy(_._2._2) map (_.toString)), "\n")
}



//  val countForHour = days.par flatMap {
//    case (month, day) => {
//      val date = new DateMidnight(2013, month, day)
//      val dayOfWeek:String = date.toString("EEEE")
//
//      hours map { hour =>
//        val resource = Resource.fromInputStream(new GZIPInputStream(
//          s3.getObject("ophan-logs", s"2013/$month/$day/$hour.gz").getObjectContent))
//        val probableSuperPixieClicks = resource.reader.lines().filter(_.contains("""ref":"http://www.google"""))
//        Map((date.getDayOfWeek(), hour) -> probableSuperPixieClicks.size)
//      }
//    }
//  } reduce (_ |+| _)
//
//  countForHour.toList.sortBy{
//    case ((day, hour), _)=> day * 24 + hour
//  } map {
//    case ((day, hour), count)=> {
//      val dayOfWeek = day match {
//        case SUNDAY => "Sunday"
//        case MONDAY => "MONDAY"
//        case TUESDAY => "TUESDAY"
//        case WEDNESDAY => "WEDNESDAY"
//        case THURSDAY => "THURSDAY"
//        case FRIDAY => "FRIDAY"
//        case SATURDAY => "SATURDAY"
//      }
//      s"$dayOfWeek, $hour, ${count / (4 * 60)}"
//    }
//  } foreach(println)

//  val probableSuperPixieClicks = resource.reader.lines().filter(_.contains(".nf-pixie"))

//  println(probableSuperPixieClicks.size)
