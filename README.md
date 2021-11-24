# Exemplo de uso das funções nativas do Kotlin para programação assincrona
## Corroutine
No Android, as corrotinas ajudam a gerenciar tarefas de longa duração que podem bloquear a linha de execução principal e fazer com que seu app pare de responder. Mais de 50% dos desenvolvedores profissionais que usam corrotinas notaram um aumento na produtividade. Este tópico descreve como você pode usar corrotinas do Kotlin para resolver esses problemas, permitindo criar um código de app mais simples e conciso.

### Consumindo Corroutines
```sh
fun login(username: String, token: String) {
        // Create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val jsonBody = "{ username: \"$username\", token: \"$token\"}"
            loginRepository.makeLoginRequest(jsonBody)
        }
    }
```
Vamos separar o código de corrotinas na função login:

- viewModelScope é um CoroutineScope predefinido que está incluído nas extensões KTX ViewModel. Todas as corrotinas precisam ser executadas em um escopo. Um CoroutineScope gerencia uma ou mais corrotinas relacionadas.
- launch é uma função que cria uma corrotina e envia a execução do corpo funcional para o agente correspondente.
- Dispatchers.IO indica que essa corrotina deve ser executada em uma linha de execução reservada para operações de E/S.
A função login é executada da seguinte maneira:

O aplicativo chama a função login da camada View na linha de execução principal.
launch cria uma nova corrotina, e a solicitação de rede é feita independentemente em uma linha de execução reservada para operações de E/S.
Enquanto a corrotina está em execução, a função login continua a execução e retorna, possivelmente antes que a solicitação de rede seja concluída. Para simplificar, a resposta da rede é ignorada por enquanto.
Como essa corrotina é iniciada com viewModelScope, ela é executada no escopo do ViewModel. Se o ViewModel for destruído porque o usuário está navegando para fora da tela, viewModelScope será cancelado automaticamente, e todas as corrotinas em execução também serão canceladas.

### Produzindo funções suspensas 
Consideramos uma função muito segura quando ela não bloqueia atualizações da IU na linha de execução principal. A função makeLoginRequest não é muito segura, porque chamar makeLoginRequest da linha de execução principal bloqueia a IU. Use a função withContext() da biblioteca de corrotinas para mover a execução de uma corrotina para uma linha de execução diferente:
```sh
class LoginRepository(...) {
    ...
    suspend fun makeLoginRequest(
        jsonBody: String
    ): Result<LoginResponse> {
        // Move the execution of the coroutine to the I/O dispatcher
        return withContext(Dispatchers.IO) {
            // Blocking network request code
        }
    }
}
```
withContext(Dispatchers.IO) move a execução da corrotina para uma linha de execução de E/S, tornando nossa função de chamada muito segura e permitindo que a IU seja atualizada conforme necessário.

makeLoginRequest também é marcado com a palavra-chave suspend. Essa palavra-chave é a maneira do Kotlin impor uma função a ser chamada de dentro de uma corrotina.

## Flow
Em corrotinas, um fluxo é um tipo que pode emitir vários valores sequencialmente, ao contrário das funções de suspensão, que retornam somente um valor. Por exemplo, você pode usar um fluxo para receber atualizações em tempo real de um banco de dados.

Um fluxo é muito semelhante a um Iterator que produz uma sequência de valores, mas usa funções de suspensão para produzir e consumir valores de maneira assíncrona. Isso significa, por exemplo, que o fluxo pode fazer uma solicitação de rede com segurança para produzir o próximo valor sem bloquear a linha de execução principal.

Há três entidades envolvidas em streams de dados:
- Um produtor produz dados que são adicionados ao stream. Graças às corrotinas, os fluxos também podem produzir dados de maneira assíncrona.
- Intermediários (opcionais) podem modificar cada valor emitido para o stream ou o próprio stream.
- Um consumidor consome os valores do stream.

### Produtor
```sh
class NewsRepository(
    private val newsApi: NewsApi,
    private val refreshIntervalMs: Long = 5000
) {
    val latestNews: Flow<List<ArticleHeadline>> = flow {
        while(true) {
            val latestNews = newsApi.fetchLatestNews()
            emit(latestNews) // Emits the result of the request to the flow
            delay(refreshIntervalMs) // Suspends the coroutine for some time
        }
    }
}
```

### Intermediário
```sh
val favoriteLatestNews: Flow<List<ArticleHeadline>> =
        latestNews
            // Intermediate operation to filter the list of favorite topics
            .map { news -> news.filter { userData.isFavoriteTopic(it) } }
            // Intermediate operation to save the latest news in the cache
            .onEach { news -> saveInCache(news) }
```

### Consumidor
```sh
class LatestNewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Trigger the flow and consume its elements using collect
            newsRepository.favoriteLatestNews.collect { favoriteNews ->
                // Update View with the latest favorite news
            }
        }
    }
}
```