
import java.util
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Mihail on 05.07.15.
 */



object Extensions {
  implicit class StringExtension(s: String) {
    def containsOneSpace(): Boolean = {
      s.contains(' ') && (s.lastIndexOf(' ') == s.indexOf(' '))
    }

  }
}