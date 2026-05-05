pipeline {
  agent {label 'xretorch-agent'}
  environment {
    SELENOID_PRESENT = "TRUE"
    SUT_LOCATION = "$WORKSPACE/"
    SCRIPTS_FOLDER = "$WORKSPACE/.retorch/scripts"
  } // EndEnvironment
  options {
    disableConcurrentBuilds()
  } // EndPipOptions
  stages {
    stage('Clean Workspace') {
        steps {
            cleanWs()
        } // EndStepsCleanWS
    } // EndStageCleanWS
    stage('Clone Project') {
        steps {
            checkout scm
        } // EndStepsCloneProject
    } // EndStageCloneProject
    stage('SETUP-Infrastructure') {
        steps {
            sh 'chmod +x -R $SCRIPTS_FOLDER'
            sh '$SCRIPTS_FOLDER/coilifecycles/coi-setup.sh'
        } // EndStepsSETUPINF
    } // EndStageSETUPInf
    stage('Stage 0') {
      failFast false
      parallel {
        stage('tjoba IdResource: frontend pet web-browser ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjoba 0 http://tjoba-api-gateway:8080'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjoba 0 http://tjoba-api-gateway:8080 "TestPets#testAddPet"'
            }// EndExecutionStageErrortjoba
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjoba 0'
          }// EndStepstjoba
        }// EndStagetjoba
        stage('tjobb IdResource: frontend visit web-browser ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobb 0 http://tjobb-api-gateway:8080'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobb 0 http://tjobb-api-gateway:8080 "TestVisits#testAddVisit"'
            }// EndExecutionStageErrortjobb
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobb 0'
          }// EndStepstjobb
        }// EndStagetjobb
        stage('tjobc IdResource: frontend owner web-browser ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobc 0 http://tjobc-api-gateway:8080'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobc 0 http://tjobc-api-gateway:8080 "TestOwners#testOwnerCreationAndSearch,TestOwners#testOwnerEdit"'
            }// EndExecutionStageErrortjobc
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobc 0'
          }// EndStepstjobc
        }// EndStagetjobc
        stage('tjobd IdResource: frontend vet web-browser ') {
          steps {
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-setup.sh tjobd 0 http://tjobd-api-gateway:8080'
              sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-testexecution.sh tjobd 0 http://tjobd-api-gateway:8080 "TestVets#testVetList"'
            }// EndExecutionStageErrortjobd
            sh '$SCRIPTS_FOLDER/tjoblifecycles/tjob-teardown.sh tjobd 0'
          }// EndStepstjobd
        }// EndStagetjobd
      } // End Parallel
    } // End Stage
stage('TEARDOWN-Infrastructure') {
      failFast false
      steps {
          sh '$SCRIPTS_FOLDER/coilifecycles/coi-teardown.sh'
      } // EndStepsTearDownInf
} // EndStageTearDown
} // EndStagesPipeline
post {
    always {
        archiveArtifacts artifacts: 'artifacts/*.csv', onlyIfSuccessful: true
        archiveArtifacts artifacts: 'target/testlogs/**/*.*', onlyIfSuccessful: false
        archiveArtifacts artifacts: 'target/containerlogs/**/*.*', onlyIfSuccessful: false
    }// EndAlways
} // EndPostActions
} // EndPipeline
