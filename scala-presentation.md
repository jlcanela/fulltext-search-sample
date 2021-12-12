https://scastie.scala-lang.org/R2b8FpxCRX6u96wJx4hM8g

// println("hello world")

var a = 1
// println(s"a = ${a}")

a = 2
// println(s"a = ${a}")

val b = 1
// println(s"b = ${b}")

def badFunction = b * 2
// println(s"badFunction = ${badFunction}")

def goodFunction(v: Int) = v * 2
// println(s"goodFunction = ${goodFunction(b)}")

def goodFunction2(v: Int) = {
  v + 1
  v * 10
  v * 2
}

// println(s"goodFunction2 = ${goodFunction2(b)}")

case class Person(firstName: String, lastName: String)

val johnDoe = Person("John", "Doe")
// println(johnDoe)

// println(s"firstName of JohnDoe is '${johnDoe.firstName}'")

val sarahConnor = Person("Sarah", "Connor")
val persons = List(johnDoe, sarahConnor)

// println(persons.mkString("<1|", "|2|", "|3>"))

def displayablePerson(p: Person): String = s"${p.firstName} ${p.lastName}"

// println(persons.map(displayablePerson).mkString(", "))

def isTheRightPerson(p: Person) = p.firstName == "Sarah" && p.lastName == "Connor"

val myPersons = persons.filter(isTheRightPerson).map(displayablePerson)
// List[Person]
// filter: Person => Boolean
// filter: List[Person] => (Person => Boolean) => List[Person]

// println(myPersons.mkString(", "))

// a collection of Persons: List[Person]
// filter : List[Person] => List[Person]
// f: (p: Person) => String
// List.map: List[A] =>  ((A) => B) => List[B]

def myPersons2(persons: List[Person]) = persons.filter(isTheRightPerson).map(displayablePerson)

val myPersons3 = (persons: List[Person]) => persons.filter(isTheRightPerson).map(displayablePerson)
val myPerson4 = myPersons2 _

def filterTheRightPerson(p: List[Person]) = p.filter(isTheRightPerson)
def makeDisplayable(p: List[Person]) = p.map(displayablePerson)

// println(makeDisplayable(filterTheRightPerson(persons)))

val myNewFunction = filterTheRightPerson andThen makeDisplayable

//println(s"myNewFunction: ${myNewFunction(persons)}")

// persons: List[Person]

val myNumbers = List(1, 2, 3)

// println(myNumbers.sum)

val l = List(2, 5, 3, 6, 4, 7)

// 2, 5 => 5
// 5, 3 => 5
// 5, 6 => 6
// 6, 4 => 6
// 6, 7 => 7
val maxResult = l.reduce((x, y) => x max y)

val sumResult = l.reduce((x, y) => x + y)

// println(l.sum)
// println(sumResult)

// def foldLeft[B](z: B)(op: (B, Person) => B): B
def countPersons(p: List[Person]): Int = p.foldLeft(0)((acc: Int, p: Person) => acc + 1)

// John Doe, Sarah Connor
// acc : 0
// (0, "John Doe") => 0 + 1 (= 1)
// (1, "Sarah Connor") => 1 + 1 (= 2)
// count = 2 

// println(countPersons(persons))

// how to count the number of firstNames in a list of Person

//val map: Map[String, Int] = Map()

val groupedPersons = persons.groupBy((p: Person) => p.firstName)
// println(groupedPersons.map( (tuple) => (tuple._1, tuple._2.size)))


case class AccessLog(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String)

val data1 = """109.169.248.247 - - [12/Dec/2015:18:25:11 +0100] "GET /administrator/ HTTP/1.1" 200 4263 "-" "Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0" "-""""
// println(data1)

val R = """^(?<ip>[0-9.]+) (?<identd>[^ ]) (?<user>[^ ]) \[(?<datetime>[^\]]+)\] \"(?<request>[^\"]*)\" (?<status>[^ ]*) (?<size>[^ ]*) \"(?<referer>[^\"]*)\" \"(?<useragent>[^\"]*)\" \"(?<unk>[^\"]*)\"""".r

// println(R.unapplySeq(data1).get.mkString(","))

// Optional Value in scala

// List : can have 0, 1, or any number of values

// Type ~ Data Structure which can have 0 or 1 value

// Option[A]
// - Some[A](value)
// - None[A]()   <- no value


// Regular exrpession, not use unapplySeq ? 

case class AccessLog(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String)

object AccessLog {
    val R = """^(?<ip>[0-9.]+) (?<identd>[^ ]) (?<user>[^ ]) \[(?<datetime>[^\]]+)\] \"(?<request>[^\"]*)\" (?<status>[^ ]*) (?<size>[^ ]*) \"(?<referer>[^\"]*)\" \"(?<useragent>[^\"]*)\" \"(?<unk>[^\"]*)\"""".r

    def unnapply(s: String) = R.findFirstMatchIn(data1) match {
      case Some(Seq(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String)) => Some(AccessLog(ip, ident, user, datetime, request, status, size, referer, userAgent, unk))
      case None => None
    }

}


R.findFirstMatchIn(data1) match {
  case Some(arr) => println(s"OK: $arr")
  case None => println("not found")
}



// count the number of firstNames in a list of Person using fold and map


 persons.groupBy(_.firstName).mapValues(_.size)

 persons.foldLeft(Map[String, Int]())( (acc, p) => acc ++ Map(p.firstName -> (acc.get(p.firstName).getOrElse(0) + 1)))

    def fromString(s: String) = s match { 
        case R(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String) => Some(AccessLog(ip, ident, user, datetime, request, status, size, referer, userAgent, unk))
        case _ => None 
    }



suggestion of exercices 
- practice with regexs

questions/details
- why use dataframe / dataset / sql ?
- relationship between option / flatMap ? 
