package com.stonesoupprogramming.wedding

import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.boot.spi.MetadataImplementor
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.reflections.Reflections
import javax.persistence.Entity
import javax.persistence.MappedSuperclass

fun generateDDL(packageName : String, properties : String = "hibernate.properties", directory : String = System.getProperty("user.dir")){
    val metadata = MetadataSources(StandardServiceRegistryBuilder().loadProperties(properties).build())

    Reflections(packageName).apply {
        getTypesAnnotatedWith(MappedSuperclass::class.java).forEach { metadata.addAnnotatedClass(it) }
        getTypesAnnotatedWith(Entity::class.java).forEach { metadata.addAnnotatedClass(it) }
    }

    SchemaExport(metadata.buildMetadata() as MetadataImplementor).apply {
        setDelimiter(";")
        setOutputFile(directory + "ddl.sql")
        setFormat(true)
        execute(true, true, false, true)
    }
}

fun main(args: Array<String>) {
    generateDDL("com.stonesoupprogramming.wedding")
}
