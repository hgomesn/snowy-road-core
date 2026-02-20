# 🚀 Criando um novo App a partir do `main`

Este repositório contém o código base do jogo.\
Todo novo aplicativo deve ser criado a partir da branch `main`.

------------------------------------------------------------------------

## 📌 1. Criar a branch de release

Padrão obrigatório:

    release/nome-do-personagem

### Comandos:

``` bash
git checkout main
git pull origin main
git checkout -b release/nome-do-personagem
git push -u origin release/nome-do-personagem
```

------------------------------------------------------------------------

# ⚙️ 2. Alterar o `applicationId`

Arquivo:

    app/build.gradle

Altere:

``` gradle
defaultConfig {
    applicationId "com.seuprojeto.nomedompersonagem"
}
```

Regras: - Letras minúsculas - Sem espaços
Clique em **Sync Now**.

------------------------------------------------------------------------

# 📦 3. Alterar o nome do pacote (Package Name)

Se o package for diferente do novo `applicationId`, altere também.

### No Android Studio:

1.  Vá em:

        app > java > com.seuprojeto.base

2.  Clique com botão direito no pacote

3.  Escolha:

        Refactor > Rename

4.  Selecione:

        Rename package

5.  Altere para:

        com.seuprojeto.nomedompersonagem

6.  Confirme o refactor

⚠️ Não renomeie manualmente as pastas. Use sempre o **Refactor** para
evitar erros.

------------------------------------------------------------------------

# 🎨 4. Alterar o nome do App

Arquivo:

    app/src/main/res/values/strings.xml

Altere:

``` xml
<string name="app_name">Nome do Personagem</string>
```

------------------------------------------------------------------------

# 🖼️ 5. Alterar o ícone do App

1.  Clique com botão direito em:

        app > res

2.  Vá em:

        New > Image Asset

3.  Selecione o novo ícone

4.  Finalize

O Android Studio atualizará automaticamente os arquivos em:

    mipmap/

------------------------------------------------------------------------

✅ Após concluir todos os passos, execute o projeto para validar se o
build está funcionando corretamente.
