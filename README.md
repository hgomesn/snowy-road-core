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

# 💰 6. Alterar os códigos do AdMob

Cada novo aplicativo deve utilizar seus próprios IDs do AdMob.

## 🔹 6.1 Criar o App no AdMob

1. Acesse https://admob.google.com
2. Clique em **Apps**
3. Clique em **Add App**
4. Escolha:
   - Plataforma: **Android**
   - Informe se o app já está publicado ou não
5. Informe o nome do novo app
6. Clique em **Add**

O AdMob irá gerar um:

- **App ID** (ca-app-pub-xxxxxxxx~xxxxxxxx)
- IDs de blocos de anúncio (Ad Unit ID)

---

## 🔹 6.2 Criar os blocos de anúncio

1. Dentro do app criado, clique em **Ad Units**
2. Clique em **Create Ad Unit**
3. Escolha o tipo:
   - Banner
   - Interstitial
   - Rewarded
4. Nomeie o bloco
5. Salve

O AdMob irá gerar um código no formato:

```
ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
```

---

## 🔹 6.3 Atualizar o App ID no Android

Abra:

```
app/src/main/AndroidManifest.xml
```

Localize:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-xxxxxxxx~xxxxxxxx"/>
```

Substitua pelo novo **App ID**.

---

## 🔹 6.4 Atualizar os Ad Unit IDs no código

Localize onde os anúncios são inicializados no projeto.

Exemplo:

```kotlin
adView.adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
```

Substitua pelo novo ID correspondente ao novo aplicativo.

---

⚠️ Importante:

- Nunca reutilize IDs de outro app.
- Nunca publique usando IDs de teste.
- Use IDs de teste apenas durante desenvolvimento.

---

✅ Após concluir todos os passos, execute o projeto para validar se o build está funcionando corretamente.

---
# 🛠️ 7. Gerar ícones e imagens para publicação

Para gerar os ícones e imagens necessárias para publicação na Google Play, recomenda-se utilizar a ferramenta:

👉 https://romannurik.github.io/AndroidAssetStudio/

Essa ferramenta permite gerar:

- Ícone do app (launcher icon)
- Ícone adaptativo (Adaptive Icon)
- Ícone para notificações
- Feature Graphic
- Outros assets necessários

---

## 🔹 Como usar

1. Acesse o site
2. Escolha o tipo de asset desejado
3. Faça upload da imagem base
4. Ajuste padding, background e formato
5. Baixe o pacote gerado

---

## 🔹 Onde usar essas imagens

As imagens geradas serão utilizadas em:

- Android Studio (ícone do app)
- Google Play Console:
  - Ícone de alta resolução
  - Feature Graphic
  - Screenshots
  - Assets promocionais

---

⚠️ Sempre gerar imagens específicas para cada novo app.  
Não reutilizar imagens de outro personagem/aplicativo.
