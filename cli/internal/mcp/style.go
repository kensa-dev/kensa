package mcp

type StyleProfile struct {
	Framework     string             `json:"framework"`
	Fixtures      []FixtureContainer `json:"fixtures"`
	MatcherFields []MatcherFieldUse  `json:"matcherFields"`
	StubHelpers   []StubHelper       `json:"stubHelpers"`
	Conventions   Conventions        `json:"conventions"`
	Exemplar      Exemplar           `json:"exemplar"`
}

func scanStyle(dir string) (StyleProfile, error) {
	return StyleProfile{
		Framework:     detectFramework(dir),
		Fixtures:      scanFixtures(dir),
		MatcherFields: scanMatcherFields(dir),
		StubHelpers:   scanStubHelpers(dir),
		Conventions:   scanConventions(dir),
		Exemplar:      pickExemplar(dir),
	}.normalised(), nil
}

// normalised replaces nil lists with empty ones. A nil Go slice marshals to
// JSON null, which fails validation against the array-typed output schema, and
// a profile read back from an older cache file can carry nulls.
func (p StyleProfile) normalised() StyleProfile {
	if p.Fixtures == nil {
		p.Fixtures = []FixtureContainer{}
	}
	for i := range p.Fixtures {
		if p.Fixtures[i].Names == nil {
			p.Fixtures[i].Names = []string{}
		}
	}
	if p.MatcherFields == nil {
		p.MatcherFields = []MatcherFieldUse{}
	}
	for i := range p.MatcherFields {
		if p.MatcherFields[i].Paths == nil {
			p.MatcherFields[i].Paths = []string{}
		}
	}
	if p.StubHelpers == nil {
		p.StubHelpers = []StubHelper{}
	}
	return p
}
