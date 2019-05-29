import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

inline fun <reified T : Task> TaskContainer.withTypeIfPresent(name: String, cfg: T.() -> Unit): T?
        = withType(T::class.java).findByName(name)?.apply(cfg)
