SHELL := /bin/bash

.DEFAULT_GOAL := build

.PHONY: clean
clean:
	./gradlew clean

.PHONY: build
build:
	./gradlew build publishToMavenLocal

.PHONY: build-ci
build-ci:
	./gradlew check --build-cache --no-daemon

.PHONY: tag-if-release
tag-if-release:
	$(eval COMMITTED_FILES:=$(shell git diff-tree --no-commit-id --name-only -r HEAD))
	@if [[ "$(COMMITTED_FILES)" = *version.txt* ]]; then\
		$(eval VERSION:=$(shell cat version.txt))\
		echo "New version $(VERSION) was committed - creating tag.";\
		git config user.name github-actions;\
		git config user.email github-actions@github.com;\
		git remote set-url origin https://x-access-token:$(GH_TOKEN)@github.com/${GITHUB_REPOSITORY}.git;\
		git tag -a "$(VERSION)" -m "Version $(VERSION)";\
		git push origin "$(VERSION)";\
	else\
		echo "New version was not committed - skipping tag creation.";\
	fi

.PHONY: publish-to-sonatype-staging
publish-to-sonatype:
	./gradlew --build-cache --no-daemon publishToSonatype closeAndReleaseSonatypeStagingRepository\
		-Psign=true \
		-PreleaseVersion="$(GIT_TAG_NAME)" \
		-PsigningKeyId="$${SIGNING_KEY_ID}" \
		-PsigningKey="$${SIGNING_KEY}" \
		-PsigningPassword="$${SIGNING_PASSWORD}" \
		-PnexusUsername="$${NEXUS_USERNAME}" \
		-PnexusPassword="$${NEXUS_PASSWORD}"

.PHONY: publish-to-sonatype-snapshot
publish-to-sonatype-snapshot:
	./gradlew --build-cache --no-daemon publishToSonatype \
		-Psign=true \
		-PsigningKeyId="$${SIGNING_KEY_ID}" \
		-PsigningKey="$${SIGNING_KEY}" \
		-PsigningPassword="$${SIGNING_PASSWORD}" \
		-PnexusUsername="$${NEXUS_USERNAME}" \
		-PnexusPassword="$${NEXUS_PASSWORD}"

.PHONY: create-release-note
create-release-note:
	@echo "Creating Release Note"
	@echo "Changelog:" > RN.md
	@sed -n "/^### v${GIT_TAG_NAME}$$/,/###/p" CHANGELOG.md | sed '1d' | sed '$$d' | sed '$$d' >> RN.md