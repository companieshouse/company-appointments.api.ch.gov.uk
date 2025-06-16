artifact_name       := company-appointments.api.ch.gov.uk
version             := unversioned

dependency_check_runner := 416670754337.dkr.ecr.eu-west-2.amazonaws.com/dependency-check-runner

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(artifact_name).jar
	rm -f ./$(artifact_name)-*.zip
	rm -rf ./build-*
	rm -rf ./build.log-*

.PHONY: test
test: test-integration test-unit

.PHONY: test-unit
test-unit: clean
	mvn verify -Dskip.unit.tests=false -Dskip.integration.tests=false

.PHONY: test-integration
test-integration: clean
	mvn verify -Dskip.unit.tests=true -Dskip.integration.tests=false

.PHONY: coverage
coverage:
	mvn verify

.PHONY: verify
verify: test-unit test-integration

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	mvn org.codehaus.mojo:versions-maven-plugin:2.17.1:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	$(info Packaging version: $(version))
	@test -s ./$(artifact_name).jar || { echo "ERROR: Service JAR not found"; exit 1; }
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./start.sh $(tmpdir)
	cp ./routes.yaml $(tmpdir)
	cp ./$(artifact_name).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: build
build:
	mvn org.codehaus.mojo:versions-maven-plugin:2.17.1:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn package -Dmaven.test.skip=true
	cp ./target/$(artifact_name)-$(version).jar ./$(artifact_name).jar

.PHONY: build-container
build-container: build
	docker build .

.PHONY: dist
dist: clean build package coverage

.PHONY: sonar
sonar:
	mvn sonar:sonar

.PHONY: sonar-pr-analysis
sonar-pr-analysis:
	mvn sonar:sonar -P sonar-pr-analysis

.PHONY: dependency-check
dependency-check:
	docker run --rm -e DEPENDENCY_CHECK_SUPPRESSIONS_HOME=/opt -v "$$(pwd)":/app -w /app ${dependency_check_runner} --repo-name="$(basename "$$(pwd)")"
.PHONY: security-check
security-check: dependency-check
