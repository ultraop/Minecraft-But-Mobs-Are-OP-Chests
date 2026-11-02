plugins { java }

allprojects {
    group = "in.ultraop.mobchests"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")
    }
}
