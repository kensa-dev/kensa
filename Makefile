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
# Tags the current `version.txt` value if no such tag exists yet. Robust to multi-commit
# pushes (the previous diff-against-HEAD approach only inspected the topmost commit, so a
# follow-up commit on top of the version bump silently lost the trigger).
tag-if-release:
	@VERSION=$$(cat version.txt); \
	if [[ "$$VERSION" == *-SNAPSHOT ]]; then \
		echo "Version $$VERSION is a SNAPSHOT — skipping tag creation."; \
	elif git rev-parse --verify --quiet "refs/tags/$$VERSION" >/dev/null; then \
		echo "Tag $$VERSION already exists — skipping."; \
	else \
		echo "Version $$VERSION not yet tagged — creating tag."; \
		git config user.name github-actions; \
		git config user.email github-actions@github.com; \
		git remote set-url origin https://x-access-token:$(GH_TOKEN)@github.com/$${GITHUB_REPOSITORY}.git; \
		git tag -a "$$VERSION" -m "Version $$VERSION"; \
		git push origin "$$VERSION"; \
	fi

.PHONY: publish-to-sonatype
publish-to-sonatype:
	./gradlew --build-cache --no-daemon publish \
		-PreleaseVersion="${RELEASE_VERSION}"
	./gradlew --build-cache --no-daemon jreleaserDeploy \
		-PreleaseVersion="${RELEASE_VERSION}"

.PHONY: publish-to-sonatype-snapshot
publish-to-sonatype-snapshot:
	./gradlew --build-cache --no-daemon publish \
		-PreleaseVersion="${RELEASE_VERSION}"
	./gradlew --build-cache --no-daemon jreleaserDeploy \
		-PreleaseVersion="${RELEASE_VERSION}"

.PHONY: create-release-note
create-release-note:
	@echo "Creating Release Note"
	@echo "Changelog:" > RN.md
	@sed -n "/^### v${GIT_TAG_NAME}[[:space:]]*.*$$/,/###/p" CHANGELOG.md | sed '1d' | sed '$$d' | sed '$$d' >> RN.md

.PHONY: copy-shell-resources
copy-shell-resources:
	@if [ ! -f ui/build/js/kensa.js ]; then \
		echo "ui/build/js/kensa.js missing — run './gradlew :ui:viteBuild' first."; exit 1; \
	fi
	cp ui/build/js/kensa.js cli/internal/shell/embed/kensa.js
	cp ui/public/logo.svg cli/internal/shell/embed/logo.svg

.PHONY: build-cli
build-cli: copy-shell-resources
	cd cli && go mod tidy && mkdir -p build/bin && go build -ldflags "-X main.version=$$(cat ../version.txt)" -o build/bin/kensa ./cmd/kensa
