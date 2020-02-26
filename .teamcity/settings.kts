import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.BuildFailureOnMetric
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.failOnMetricChange
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {

    vcsRoot(HttpsGerritOnapOrgRClampGitRefsHeadsMaster)

    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(HttpsGerritOnapOrgRClampGitRefsHeadsMaster)
    }

    steps {
        maven {
            name = "Build"
            goals = "clean install"
            mavenVersion = bundled_3_3()
            userSettingsSelection = "settings.xml"
            jdkHome = "%env.JDK_11_x64%"
        }
        maven {
            name = "Sonar"
            goals = "sonar:sonar"
            runnerArgs = "-Dsonar.projectKey=Clamp -Dsonar.host.url=http://10.0.0.151:31832 -Dsonar.login=6357321d2aac61d1667ac211ddf0dba71ebca022"
            userSettingsSelection = "settings.xml"
            jdkHome = "%env.JDK_11_x64%"
            coverageEngine = jacoco {
                classLocations = "target"
            }
        }
    }

    triggers {
        vcs {
        }
        schedule {
            schedulingPolicy = daily {
                hour = 14
            }
            branchFilter = ""
            triggerBuild = always()
        }
    }

    failureConditions {
        failOnMetricChange {
            metric = BuildFailureOnMetric.MetricType.TEST_COUNT
            threshold = 20
            units = BuildFailureOnMetric.MetricUnit.PERCENTS
            comparison = BuildFailureOnMetric.MetricComparison.LESS
            compareTo = build {
                buildRule = lastSuccessful()
            }
        }
    }
})

object HttpsGerritOnapOrgRClampGitRefsHeadsMaster : GitVcsRoot({
    name = "https://gerrit.onap.org/r/clamp.git#refs/heads/master"
    url = "https://gerrit.onap.org/r/clamp.git"
    branchSpec = """
        +:refs/changes/*
        +:refs/heads/*
    """.trimIndent()
})
