import { describe, expect, it } from "vitest"
import { setStateFilter } from "./stateFilterToggle"

describe("setStateFilter", () => {
    it("adds state token to empty query", () => {
        expect(setStateFilter("", "passed")).toBe("state:passed")
    })

    it("clears the filter when the same state is reapplied", () => {
        expect(setStateFilter("state:passed", "passed")).toBe("")
    })

    it("replaces an existing state filter with a different one", () => {
        expect(setStateFilter("state:passed", "failed")).toBe("state:failed")
    })

    it("clears all other state tokens when activating a different state", () => {
        expect(setStateFilter("state:passed state:failed", "disabled")).toBe("state:disabled")
    })

    it("preserves non-state tokens when adding", () => {
        expect(setStateFilter("issue:BUG-1 foo", "passed")).toBe("issue:BUG-1 foo state:passed")
    })

    it("preserves non-state tokens when toggling off", () => {
        expect(setStateFilter("issue:BUG-1 state:passed foo", "passed")).toBe("issue:BUG-1 foo")
    })

    it("preserves non-state tokens when swapping state", () => {
        expect(setStateFilter("pkg:com.example state:passed hello", "failed"))
            .toBe("pkg:com.example hello state:failed")
    })

    it("collapses internal and surrounding whitespace", () => {
        expect(setStateFilter("  foo   bar  ", "passed")).toBe("foo bar state:passed")
    })
})
