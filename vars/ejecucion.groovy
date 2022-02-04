def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('NEXUS-USER')
            NEXUS_PASSWORD     = credentials('NEXUS-PASS')
        }
        parameters {
            choice( name:'compileTool', choices: ['Maven', 'Gradle'], description: 'Seleccione herramienta de compilacion' )
            string( defaultValue: '', name: 'stages', trim: true )
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                    def ci_or_cd = verifyBranchName()
                    switch(params.compileTool)
                        {
                            case 'Maven':
                                figlet 'Ejecución con Maven'
                                maven.call(verifyBranchName(), params.stages)
                            break;
                            case 'Gradle':
                                figlet 'Ejecución con Gradle'
                                gradle.call(verifyBranchName(), params.stages)
                            break;
                        }
                    }
                }
                post{
                    success{
                        slackSend color: 'good', message: "[Diego Inostroza] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins-slack'
                    }
                    failure{
                        slackSend color: 'danger', message: "[Diego Inostroza] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins-slack'
                    }
                }
            }
        }
    }
}


def verifyBranchName(){
	if(env.GIT_BRANCH.contains('feature-') || env.GIT_BRANCH.contains('develop-')) {
		return 'CI'
	} else {
		return 'CD'
	}
	return 
}

return this;