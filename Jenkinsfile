node('xretorch-agent') {
	stage("Init")  {
		deleteDir()
		checkout scm
	}
	stage("Test") {
		sh "mvn test-compile -U --no-transfer-progress"
		junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
	}
}
