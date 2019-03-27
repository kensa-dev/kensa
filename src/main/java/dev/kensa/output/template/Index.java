package dev.kensa.output.template;

class Index {
    private final String content;

    Index(String content) {
        this.content = content;
    }

    @SuppressWarnings("unused") // Used by pebble template
    public String getContent() {
        return content;
    }
}
