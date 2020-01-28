addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

credentials += Credentials(

 /* "Sonatype Nexus Repository Manager",
  "deino.at-internal.com",
  "Brenda",
  "Brenda123"*/

  System.getenv("AT_RESOLVER_REALM"),
  System.getenv("AT_RESOLVER_HOST"),
  System.getenv("AT_RESOLVER_USER"),
  System.getenv("AT_RESOLVER_PASS")
)