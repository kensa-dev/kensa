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
# Tags the current `version.txt` value iff this push changed it. Multi-commit pushes are
# handled via $BEFORE_SHA (github.event.before) so the trigger isn't lost when the version
# bump is followed by further commits in the same push. version.txt is the release stream
# and never carries a SNAPSHOT suffix — snapshots live in snapshot-version.txt.
tag-if-release:
	@VERSION=$$(cat version.txt); \
	BEFORE=$${BEFORE_SHA:-}; \
	if [[ -z "$$BEFORE" || "$$BEFORE" =~ ^0+$$ ]]; then \
		echo "No previous commit available — skipping tag creation."; \
	else \
		git fetch --quiet --no-tags origin "$$BEFORE" --depth=1 2>/dev/null || true; \
		if ! git diff --name-only "$$BEFORE" HEAD -- version.txt | grep -qx version.txt; then \
			echo "version.txt unchanged in this push — skipping tag creation."; \
		elif git ls-remote --exit-code --tags origin "refs/tags/$$VERSION" >/dev/null 2>&1; then \
			echo "Tag $$VERSION already exists on remote — skipping."; \
		else \
			echo "version.txt bumped to $$VERSION — creating tag."; \
			git config user.name github-actions; \
			git config user.email github-actions@github.com; \
			git remote set-url origin https://x-access-token:$(GH_TOKEN)@github.com/$${GITHUB_REPOSITORY}.git; \
			git tag -a "$$VERSION" -m "Version $$VERSION"; \
			git push origin "$$VERSION"; \
		fi; \
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
