/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU Lesser General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gw2tb.manager.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
abstract class GenerateLauncherApplicationManifest @Inject constructor(
    projectLayout: ProjectLayout
) : DefaultTask() {

    @get:Input
    abstract val assemblyName: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:OutputDirectory
    abstract val destinationDirectory: DirectoryProperty

    init {
        destinationDirectory.convention(projectLayout.buildDirectory.dir("generated-application-manifests/$name"))
    }

    @TaskAction
    protected fun run() {
        destinationDirectory.get().asFile.mkdirs()

        val resourceFile = destinationDirectory.file("manifest.rc").get().asFile
        resourceFile.writeText("""
        #include <winresrc.h>
        1 RT_MANIFEST "${assemblyName.get()}.exe.manifest"
        """.trimIndent())

        val applicationManifest = destinationDirectory.file("${assemblyName.get()}.exe.manifest").get().asFile
        applicationManifest.writeText("""
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <assembly xmlns="urn:schemas-microsoft-com:asm.v1"
                  manifestVersion="1.0"
                  xmlns:asmv3="urn:schemas-microsoft-com:asm.v3"
        >
          <assemblyIdentity
            name="${assemblyName.get()}"
            version="${version.get()}"
            processorArchitecture="X86"
            type="win32"
          />
          <description>GW2 Add-on Manager</description>

          <dependency>
            <dependentAssembly>
              <assemblyIdentity
                type="win32"
                name="Microsoft.Windows.Common-Controls"
                version="6.0.0.0"
                processorArchitecture="*"
                publicKeyToken="6595b64144ccf1df"
                language="*"
              />
            </dependentAssembly>
          </dependency>

          <trustInfo xmlns="urn:schemas-microsoft-com:asm.v3">
            <security>
              <requestedPrivileges>
                <requestedExecutionLevel
                  level="asInvoker"
                  uiAccess="false"/>
              </requestedPrivileges>
            </security>
          </trustInfo>

          <asmv3:application>
            <asmv3:windowsSettings xmlns:dpi1="https://schemas.microsoft.com/SMI/2005/WindowsSettings"
                                   xmlns:dpi2="https://schemas.microsoft.com/SMI/2016/WindowsSettings">
              <dpi1:dpiAware>true/PM</dpi1:dpiAware>
              <dpi2:dpiAwareness>PerMonitorV2, PerMonitor, system</dpi2:dpiAwareness>
            </asmv3:windowsSettings>
          </asmv3:application>

          <compatibility xmlns="urn:schemas-microsoft-com:compatibility.v1">
            <application>
              <!-- Windows Vista -->
              <supportedOS Id="{e2011457-1546-43c5-a5fe-008deee3d3f0}"/>
              <!-- Windows 7 -->
              <supportedOS Id="{35138b9a-5d96-4fbd-8e2d-a2440225f93a}"/>
              <!-- Windows 8 -->
              <supportedOS Id="{4a2f28e3-53b9-4441-ba9c-d69d4a4a6e38}"/>
              <!-- Windows 8.1 -->
              <supportedOS Id="{1f676c76-80e1-4239-95bb-83d0f6d0da78}"/>
              <!-- Windows 10 -->
              <supportedOS Id="{8e0f7a12-bfb3-4fe8-b9a5-48fd50a15a9a}"/>
            </application>
          </compatibility>
        </assembly>
        """.trimIndent())
    }

}
