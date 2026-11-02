plugins { java }

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

tasks.jar {
    archiveBaseName.set("UltraOP-MobChests")
}
