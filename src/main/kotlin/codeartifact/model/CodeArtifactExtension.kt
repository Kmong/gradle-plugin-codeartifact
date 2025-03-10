package codeartifact.model

open class CodeArtifactExtension {
    var enabled: Boolean = false
    var globalAccountId: String = ""
    var globalDomain: String = ""
    var repositories: MutableList<Repository> = mutableListOf()

    @Suppress("unused")
    fun repository(init: Repository.() -> Unit) {
        val repo = Repository()
        repo.init()
        repositories.add(repo)
    }
}