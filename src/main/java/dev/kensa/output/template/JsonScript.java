package dev.kensa.output.template;

class JsonScript {
    private final String id;
    private final String content;

    JsonScript(String id, String content) {
        this.id = id;
        this.content = content;
    }

    @SuppressWarnings("unused") // Used by pebble template
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused") // Used by pebble template
    public String getContent() {
        return content;
    }
}
