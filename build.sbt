scalaVersion := "2.11.8"

dexMaxHeap := "4g"

enablePlugins(AndroidApp)

android.useSupportVectors

name := "shadowsocksr"

applicationId := "com.sugarsvalley.svpn"

platformTarget := "android-25"

compileOrder := CompileOrder.JavaThenScala

javacOptions ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil

scalacOptions ++= "-target:jvm-1.7" :: "-Xexperimental" :: Nil

ndkJavah := Seq()

ndkBuild := Seq()

proguardVersion := "5.3.2"

proguardCache := Seq()

proguardOptions ++=
  "-keep class com.github.shadowsocks.System { *; }" ::
  "-keep class okhttp3.** { *; }" ::
  "-keep interface okhttp3.** { *; }" ::
  "-keep class okio.** { *; }" ::
  "-keep interface okio.** { *; }" ::
  "-dontwarn okio.**" ::
  "-dontwarn com.google.android.gms.internal.**" ::
  "-dontwarn com.j256.ormlite.**" ::
  "-dontwarn org.xbill.**" ::
  "-dontwarn javax.annotation.Nullable" ::
  "-dontwarn javax.annotation.ParametersAreNonnullByDefault" ::
  Nil

shrinkResources := false

typedResources := false

resConfigs := Seq("ja", "ru", "zh-rCN", "zh-rTW")

resolvers += Resolver.jcenterRepo

//resolvers += Resolver.url("jitpack", url("https://jitpack.io"))

resolvers += Resolver.url("ali_maven", url("http://maven.aliyun.com/nexus/content/groups/public/"))

resolvers += "google_extra" at "file:/Users/jishenghuo/Library/Android/sdk/extras/m2repository/"

libraryDependencies ++=
  "com.android.support" % "cardview-v7" % "25.1.0" ::
  "com.android.support" % "design" % "25.1.0" ::
  "com.android.support" % "gridlayout-v7" % "25.1.0" ::
  "com.android.support" % "preference-v14" % "25.1.0" ::
  "com.evernote" % "android-job" % "1.1.3" ::
  "com.github.clans" % "fab" % "1.6.4" ::
  "com.github.jorgecastilloprz" % "fabprogresscircle" % "1.01" ::
  "com.google.android.gms" % "play-services-analytics" % "10.0.1" ::
  "com.google.android.gms" % "play-services-gcm" % "10.0.1" ::
  "com.google.android.gms" % "play-services-ads" % "10.0.1" ::
  "com.j256.ormlite" % "ormlite-android" % "5.0" ::
  "com.mikepenz" % "fastadapter" % "2.1.5" ::
  "com.mikepenz" % "iconics-core" % "2.8.2" ::
  "com.mikepenz" % "materialdrawer" % "5.8.1" ::
  "com.mikepenz" % "materialize" % "1.0.0" ::
  "com.twofortyfouram" % "android-plugin-api-for-locale" % "1.0.2" ::
  "dnsjava" % "dnsjava" % "2.1.7" ::
  "eu.chainfire" % "libsuperuser" % "1.0.0.+" ::
  "net.glxn.qrgen" % "android" % "2.0" ::
  "com.squareup.okhttp3" % "okhttp" % "3.8.0" ::
  "com.android.support.constraint" % "constraint-layout" % "1.0.2" ::
  "com.google.code.findbugs" % "jsr305" % "1.3.+" ::
  "com.android.billingclient" % "billing" % "1.0" ::
  Nil

lazy val nativeBuild = TaskKey[Unit]("native-build", "Build native executables")

nativeBuild := {
  val logger = streams.value.log
  Process("./build.sh") ! logger match {
    case 0 => // Success!
    case n => sys.error(s"Native build script exit code: $n")
  }
}
