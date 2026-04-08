# Cryptex 🔐

A CLI tool to encrypt and decrypt files using **AES-256-GCM** — built as a hands-on study project for learning cryptography concepts in Java 21.

---

## Usage

```bash
# Encrypt (no password — generates a .key file in ~/.cryptex/keys/)
cryptex image.jpg

# Encrypt with password
cryptex image.jpg -p mypassword

# Decrypt (key-file mode — reads .key automatically)
cryptex image.jpg.cptx -d

# Decrypt with password
cryptex image.jpg.cptx -d -p mypassword
```

> **Note on key-file mode:** when encrypting without a password, a `.key` file is saved to `~/.cryptex/keys/`. This file is required to decrypt later — without it, the encrypted file cannot be recovered. For portability across machines, use password mode (`-p`) instead.

---

## Installation

### Option 1 — Download the prebuilt binary (Windows only)

Head to the [releases page](https://github.com/wthallys/Cryptex/releases/tag/v1.0.0) and download `cryptex.exe`. No Java installation required.

> **Linux / macOS:** no prebuilt binary is available yet for these platforms. Please use Option 2 to build from source.

**Windows (PowerShell as Administrator):**
```powershell
Copy-Item cryptex.exe C:\Windows\System32\cryptex.exe
```

### Option 2 — Build from source

**Prerequisites:**
- [GraalVM JDK 21](https://www.graalvm.org/downloads/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Visual C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) *(Windows only)*

```bash
mvn clean native:compile
```

The executable will be generated at `target/cryptex` (or `target/cryptex.exe` on Windows).

Then install it globally using the commands from Option 1.

After installing, `cryptex` can be called from any terminal, from any directory.

---

## Cryptography concepts

| Concept | Description |
|---|---|
| **AES-256-GCM** | Symmetric authenticated cipher. Provides both confidentiality and integrity in a single operation. |
| **PBKDF2** | Password-Based Key Derivation Function. Stretches a user password into a 256-bit AES key using 310,000 iterations of HMAC-SHA256, making brute-force attacks significantly slower. |
| **Salt** | Random value mixed into PBKDF2 before key derivation. Ensures the same password produces a different key for every file, defeating precomputed dictionary attacks. |
| **IV / Nonce** | Random 12-byte value used as the GCM counter seed. Ensures the same file encrypted twice with the same key produces completely different ciphertexts. Reusing an IV with the same key completely breaks GCM confidentiality. |
| **Authentication tag** | A 128-bit MAC computed by GCM over the ciphertext. Verified on decryption — any tampering with the encrypted file is detected before a single byte of plaintext is returned. |
| **SecureRandom** | Cryptographically secure random number generator backed by OS entropy sources. Used for all salt, IV, and key generation. |

---

## .cptx file format

Every encrypted file starts with a fixed 51-byte binary header, followed by the ciphertext.

```
┌─────────────┬────────┬─────────────────────────────────────────────────────┐
│ Field       │ Bytes  │ Description                                         │
├─────────────┼────────┼─────────────────────────────────────────────────────┤
│ MAGIC       │ 5      │ "CPTX\0" — identifies the file format               │
│ VERSION     │ 1      │ Format version (0x01)                               │
│ FLAGS       │ 1      │ Bit 0: 1 = password-based, 0 = key-file-based       │
│ SALT        │ 16     │ Random salt for PBKDF2 key derivation               │
│ IV          │ 12     │ Random IV/nonce for AES-GCM                         │
│ AUTH_TAG    │ 16     │ GCM authentication tag (integrity verification)     │
└─────────────┴────────┴─────────────────────────────────────────────────────┘
[ ciphertext follows... ]
```

The header is self-contained — everything needed to decrypt the file (except the key or password) is embedded in the file itself.

---

## Project structure

```
src/main/java/dev/cryptex/
├── Cryptex.java                  ← entry point
├── cli/
│   ├── CliParser.java            ← argument parsing and validation
│   └── Command.java              ← parsed command (record)
├── crypto/
│   ├── AesGcmCipher.java         ← AES-256-GCM encrypt/decrypt primitives
│   ├── CryptoEngine.java         ← high-level encrypt/decrypt facade
│   └── KeyDerivation.java        ← PBKDF2, salt, IV, and key generation
├── io/
│   ├── CptxHeader.java           ← binary header serialization (record)
│   └── FileProcessor.java        ← file I/O and operation routing
└── exception/
    └── CryptexException.java     ← domain exception
```

---

---

# Cryptex 🔐

Uma ferramenta CLI para criptografar e descriptografar arquivos usando **AES-256-GCM** — construída como projeto de estudo prático de conceitos de criptografia em Java 21.

---

## Uso

```bash
# Criptografar (sem senha — gera um arquivo .key em ~/.cryptex/keys/)
cryptex imagem.jpg

# Criptografar com senha
cryptex imagem.jpg -p minhasenha

# Descriptografar (modo key-file — lê o .key automaticamente)
cryptex imagem.jpg.cptx -d

# Descriptografar com senha
cryptex imagem.jpg.cptx -d -p minhasenha
```

> **Atenção no modo key-file:** ao criptografar sem senha, um arquivo `.key` é salvo em `~/.cryptex/keys/`. Esse arquivo é necessário para descriptografar depois — sem ele, o arquivo criptografado não pode ser recuperado. Para portabilidade entre máquinas, use o modo senha (`-p`).

---

## Instalação

### Opção 1 — Baixar o binário compilado (somente Windows)

Acesse a [página de releases](https://github.com/wthallys/Cryptex/releases/tag/v1.0.0) e baixe o `cryptex.exe`. Não é necessário ter Java instalado.

> **Linux / macOS:** ainda não há binário compilado disponível para essas plataformas. Use a Opção 2 para compilar a partir do código fonte.

**Windows (PowerShell como Administrador):**
```powershell
Copy-Item cryptex.exe C:\Windows\System32\cryptex.exe
```

### Opção 2 — Compilar a partir do código fonte

**Pré-requisitos:**
- [GraalVM JDK 21](https://www.graalvm.org/downloads/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Visual C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) *(somente Windows)*

```bash
mvn clean native:compile
```

O executável será gerado em `target/cryptex` (ou `target/cryptex.exe` no Windows).

Em seguida, instale globalmente usando os comandos da Opção 1.

Após a instalação, `cryptex` pode ser chamado de qualquer terminal, em qualquer diretório.

---

## Conceitos de criptografia

| Conceito | Descrição |
|---|---|
| **AES-256-GCM** | Cifra simétrica autenticada. Garante confidencialidade e integridade em uma única operação. |
| **PBKDF2** | Função de derivação de chave baseada em senha. Transforma a senha do usuário em uma chave AES de 256 bits usando 310.000 iterações de HMAC-SHA256, tornando ataques de força bruta significativamente mais lentos. |
| **Salt** | Valor aleatório misturado ao PBKDF2 antes da derivação da chave. Garante que a mesma senha produza uma chave diferente para cada arquivo, inviabilizando ataques de dicionário pré-computados. |
| **IV / Nonce** | Valor aleatório de 12 bytes usado como semente do contador GCM. Garante que o mesmo arquivo criptografado duas vezes com a mesma chave produza ciphertexts completamente diferentes. Reutilizar um IV com a mesma chave quebra completamente a confidencialidade do GCM. |
| **Auth tag** | Um MAC de 128 bits calculado pelo GCM sobre o ciphertext. Verificado na descriptografia — qualquer adulteração no arquivo é detectada antes que um único byte do conteúdo original seja retornado. |
| **SecureRandom** | Gerador de números aleatórios criptograficamente seguro, alimentado por fontes de entropia do sistema operacional. Usado para geração de salt, IV e chaves. |

---

## Formato do arquivo .cptx

Todo arquivo criptografado começa com um cabeçalho binário fixo de 51 bytes, seguido do ciphertext.

```
┌─────────────┬────────┬─────────────────────────────────────────────────────┐
│ Campo       │ Bytes  │ Descrição                                           │
├─────────────┼────────┼─────────────────────────────────────────────────────┤
│ MAGIC       │ 5      │ "CPTX\0" — identifica o formato do arquivo          │
│ VERSION     │ 1      │ Versão do formato (0x01)                            │
│ FLAGS       │ 1      │ Bit 0: 1 = baseado em senha, 0 = baseado em arquivo │
│ SALT        │ 16     │ Salt aleatório para derivação de chave (PBKDF2)     │
│ IV          │ 12     │ IV/nonce aleatório para AES-GCM                     │
│ AUTH_TAG    │ 16     │ Tag de autenticação GCM (verificação de integridade)│
└─────────────┴────────┴─────────────────────────────────────────────────────┘
[ ciphertext segue a partir daqui... ]
```

O cabeçalho é autossuficiente — tudo que é necessário para descriptografar o arquivo (exceto a chave ou senha) está embutido nele.

---

## Estrutura do projeto

```
src/main/java/dev/cryptex/
├── Cryptex.java                  ← ponto de entrada
├── cli/
│   ├── CliParser.java            ← parsing e validação dos argumentos
│   └── Command.java              ← comando parseado (record)
├── crypto/
│   ├── AesGcmCipher.java         ← primitivas AES-256-GCM
│   ├── CryptoEngine.java         ← facade de alto nível
│   └── KeyDerivation.java        ← PBKDF2, salt, IV e geração de chaves
├── io/
│   ├── CptxHeader.java           ← serialização do cabeçalho binário (record)
│   └── FileProcessor.java        ← I/O de arquivos e roteamento
└── exception/
    └── CryptexException.java     ← exceção de domínio
```