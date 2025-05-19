# korvo-to-anki-lemmatizer

[![GitHub Release](https://img.shields.io/github/v/release/dankoy/korvo-to-anki-lemmatizer)](https://github.com/Dankoy/korvo-to-anki-lemmatizer/releases/latest)
![GitHub Release Date](https://img.shields.io/github/release-date/dankoy/korvo-to-anki-lemmatizer)

[![Maven CI with Gradle](https://github.com/Dankoy/korvo-to-anki-lemmatizer/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/Dankoy/korvo-to-anki-lemmatizer/actions/workflows/maven.yml)

![Hits](https://hitscounter.dev/api/hit?url=https%3A%2F%2Fgithub.com%2FDankoy%2Fkorvo-to-anki-lemmatizer&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)
![GitHub search hit counter](https://img.shields.io/github/search/dankoy/korvo-to-anki-lemmatizer/korvo-to-anki-lemmatizer)

Lemmatizer for [korvo-to-anki](https://github.com/Dankoy/korvo-to-anki).

Uses Stanford CoreNLP lib for lemmatization https://stanfordnlp.github.io/CoreNLP/lemma.html.

# Stack

* Java 21
* Spring boot 3.3.2
* Spring shell
* Spring boot jdbc
* Liquibase
* Stanford CoreNLP

# Purpose

If you, like me, translated words as is in KOReader, then you have many words with like 'was' or '
maintained'. Also, such words can be duplicated by lemma of such words (vocabulary_builder plugin
never checks that). But anki cards has to have lemmas of such words. For example, lemma for word '
was' is 'be' and lemma for word 'maintained' is 'maintain'.

So this app checks for such words and rewrites it in lemmas.

Lemmatization works only for strings containing one word or hyphen.

# Usage

Functionality:

1) Check duplicated words and delete them in db
2) Check for words that already has lemmas in db and delete them from db.
3) Get lemmas from words and update them in db
4) Possible to keep original words after lemmatization in db. More [here](https://github.com/Dankoy/korvo-to-anki-lemmatizer/issues/29)

To correctly use this project, it's necessary to keep in mind that backups are absolutely necessary,
and automatic operations for duplicates removal should be done on your own risk. You can check
duplicates before deleting them manually or automatically. Application never tries to figure out
which word is better to be kept and which has to be deleted. It just deletes randomly if you have
multiple duplicates.

## More on duplicates

Words like 'maintained', 'maintains' are considered to be duplicates because they both have lemma
as 'maintain'. Application will take one of them and print, so you can check and manually, or you
can run command to delete one of the word automatically.

Also, you can have words like 'maintain' and 'maintained'. So you have already lemma in db, and that
means, you don't need to find lemma for word 'maintained'. Application will take such words, print
them, so you can check and delete manually, or you can run a command to delete them automatically.

## Run

#### On linux

`java -jar -Dspring.datasource.url=jdbc:sqlite:/path/to/vocabulary_builder.sqlite3 korvo-to-anki-lemmatizer.jar `

#### On windows

`java "-Dspring.datasource.url=jdbc:sqlite:\path\to\vocabulary_builder.sqlite3" -jar .\korvo-to-anki-lemmatizer.jar`

## Commands

```
lemmatize
       check-lemmas-if-exists, clie: Check if db contains lemmas that could be ignored
       * lemmatize-all-vocabularies, lav: Lemmatize vocabularies.
       Should be used only if
         1) check-on-duplicates (cod) command returns empty list
         2) check-lemmas-if-exists (clie) command returns empty list

       * auto-delete-duplicates, add: Automatically delete duplicates. Do on your own risk.
       check-on-duplicates, cod: Check lemmas on duplicates
       * auto-delete-words-lemmas-exists, adwle: Automatically delete words that already has lemmas in db for. Do on your own risk.
```

## 1. Check on exising lemmas

First check on already existing lemmas in db.

```shell
shell:>clie
17:57:38.607 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
17:57:38.949 [main] INFO  com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@113dcaf8
17:57:38.951 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
[ 
{
  "word" : "wretches",
  "lemma" : "wretch",
  "title" : "The Sorrows of Satan (Horror Classic)"
}, {
  "word" : "keenest",
  "lemma" : "keen",
  "title" : "Orcs"
} 
]
```

If the list is not empty then delete duplicates manually or go to next step.

## 2. Automatically remove words for which lemmas are present

This action cannot be undone.

```shell
shell:>adwle
18:02:05.785 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch size: 100
...
```

After deleting such words go to step 3

## 3. Check on duplicates

```shell
shell:>cod
18:03:52.271 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
18:03:52.486 [main] INFO  com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@39f0c343
18:03:52.488 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
[ {
  "word" : "lurking",
  "lemma" : "lurk",
  "title" : "The Sorrows of Satan (Horror Classic)"
}, {
  "word" : "Drapes",
  "lemma" : "drape",
  "title" : "Orcs"
}
]
```

If empty go to step 5, if not delete manually or go to step 4

## 4. Automatically delete duplicates

```shell
shell:>add
18:03:55.322 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch size: 53
...

```

After deleting got to step 5

## 5. Lemmatize your db and keep or not keep original words 

### Don't keep original words

If you don't want to keep original words then use `lav` command

```shell
shell:>lav
18:04:19.608 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch size: 100
18:04:19.608 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch: [VocabularyLemmaFullDTO[word=dripping, lemma=drip, title=Title[id=1, name=Orcs, filter=1], createTime=1658041927, reviewTime=0, dueTime=1658042227, reviewCount=0, prevContext=null, nextContext=null, streakCount=0], VocabularyLemmaFullDTO[word=marshlands, lemma=marshland, 
...
```

### Keep original words

If you want to keep original words then use `lavko` command. 

This command will keep original word and put it in nextContext in db. 
 
More details can be found [here](https://github.com/Dankoy/korvo-to-anki-lemmatizer/issues/29)

```shell
shell:>lavko
18:04:19.608 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch size: 100
18:04:19.608 [main] INFO  r.d.k.c.d.v.v.VocabularyDaoJdbc - Batch: [VocabularyLemmaFullDTO[word=dripping, lemma=drip, title=Title[id=1, name=Orcs, filter=1], createTime=1658041927, reviewTime=0, dueTime=1658042227, reviewCount=0, prevContext=null, nextContext=(dripping), streakCount=0], VocabularyLemmaFullDTO[word=marshlands, lemma=marshland, 
...
```

## Mentions

1) You can't run lemmatizing command if duplicates of any kind exists.

```shell
shell:>lav
Command 'lemmatize-all-vocabularies' exists but is not currently available because first check and fix all duplicates
Details of the error have been omitted. You can use the stacktrace command to print the full stacktrace.
```

2) Didn't publish jar file in releases because it's too fat (500 Mbs). You have to clone and build
   it yourself.

```
./mvnw clean package
```

Result jar is in target directory.

## 6. Move vocabulary builder db

Check if lemmas satisfy you, make another backup of old database.
Move updated database into your reader, check if everything works.
Run [korvo-to-anki](https://github.com/Dankoy/korvo-to-anki) app again if necessary.


