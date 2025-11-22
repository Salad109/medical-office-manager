package io.salad109.medicalofficemanager

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModulithTest {

    private val modules = ApplicationModules.of(MedicalOfficeManagerApplication::class.java)

    @Test
    fun `verify module structure`() {
        modules.verify()
    }

    @Test
    fun `generate documentation`() {
        Documenter(modules)
            .writeDocumentation()
            .writeModulesAsPlantUml()
    }
}