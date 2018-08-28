# Backlog Migration for CybozuLive
CybozuLiveのグループを[Backlog]に移行するためのツールです。
(English document is described after Japanese)

* Backlog
    * [https://backlog.com](https://backlog.com/)

## 必須要件
* **Java 8**
* Backlogの **管理者権限**

ダウンロード
------------

こちらのリンクからjarファイルをダウンロードし、以下のようにコマンドラインから実行します。

https://github.com/nulab/BacklogMigration-CybozuLive/releases

    java -jar backlog-migration-cybozulive-[最新バージョン].jar

プロキシ経由で使用する場合は、以下のように実行します。

    java -Djdk.http.auth.tunneling.disabledSchemes= -Dhttps.proxyHost=[プロキシサーバのホスト名] -Dhttps.proxyPort=[プロキシサーバのポート番号] -Dhttps.proxyUser=[プロキシユーザー名] -Dhttps.proxyPassword=[プロキシパスワード] -jar backlog-migration-cybozulive-[最新バージョン].jar
    
## 使い方
### 前準備

作業用のディレクトリを作成します。

    $ mkdir work
    $ cd work
    
jarファイルをダウンロードします。

データディレクトリを作成します。

    $ mkdir backlog-migration

### CybozuLiveからCSVファイルのエクスポート
1. 該当のグループを開き、[設定] > [エクスポート]をクリックする
2. 形式を`標準`、文字コードが`UTF-8`であることを確認し、[ダウンロード]をクリックする
    
CybozuLiveからエクスポートしたCSVファイルを、作成した`backlog-migration`ディレクトリ内に配置します。


### Init コマンド

[**init**]コマンドを実行し、CSVファイルの解析とマッピングファイルを準備する必要があります。
(マッピングファイルはCybozuLiveとBacklogのデータを対応付けるために使用します。)

    java -jar backlog-migration-cybozulive-[latest version].jar \
      init \
      --backlog.key   [BacklogのAPIキー] \
      --backlog.url   [BacklogのURL] \
      --projectKey    [Backlogプロジェクトキー]
      
サンプルコマンド：

    java -jar backlog-migration-cybozulive-[最新バージョン].jar \
      init \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxx.backlog.com \
      --projectKey BACKLOG_PROJECT

- backlog-migration/mappings/users.json (ユーザー)
- backlog-migration/mappings/priorities.json (優先度)
- backlog-migration/mappings/statuses.json (状態)

### マッピングファイルを修正
自動作成されるファイルは以下のようにCSV形式で出力されます。
Backlog側の空白の項目は自動設定できなかった項目になります。
以下のファイルからBacklog側の項目をコピーして、空白を埋める必要が有ります。

- backlog-migration/mappings/users_list.json (ユーザー)
- backlog-migration/mappings/priorities_list.json (優先度)
- backlog-migration/mappings/statuses_list.json (状態)

### Import コマンド

[**import**]コマンドを実行することでインポートを実行します。

    java -jar backlog-migration-cybozulive-[latest version].jar \
      import \
      --backlog.key   [BacklogのAPIキー] \
      --backlog.url   [BacklogのURL] \
      --projectKey    [Backlogプロジェクトキー]
      
サンプルコマンド：

    java -jar backlog-migration-cybozulive-[最新バージョン].jar \
      import \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxx.backlog.com \
      --projectKey BACKLOG_PROJECT

## 制限事項

### 実行できるユーザー
Backlogの **管理者権限** が必要になります。

### プロジェクトについて
* テキスト整形のルール： **markdown**

### チャットデータの移行について
* チャットとして移行された課題の状態は全て **Open** となります。
* 課題の作成者と作成日時は、最初にチャットルームで投稿したユーザと投稿日時になります

### CybozuLive側の制限について
- 掲示板/ToDoリスト
  - コメント数：最新から10,000件
- イベント
  - コメント数：最新から10,000件
- チャット
  - コメント数：最新から10,000件
- 掲示板やイベントの添付ファイルは移行できません
- ToDoのカテゴリは移行できません。

### Backlog側の制限について
* 空のコメントは登録されません。

## 再インポートの仕様

Backlog側に同一プロジェクトキーがある場合は、以下の仕様でインポートされます。

※ 対象のプロジェクトに参加していない場合

対象プロジェクトはインポートされず以下のメッセージが表示されます。対象プロジェクトをインポートする場合は、対象プロジェクトに参加してください。[⭕️⭕️を移行しようとしましたが⭕️⭕️に参加していません。移行したい場合は⭕️⭕️に参加してください。]

|項目|仕様|
|:-----------|------------|
|プロジェクト|同じプロジェクトキーのプロジェクトがある場合、プロジェクトを作成せず対象のプロジェクトに課題やWikiを登録します。|
|課題|件名、作成者、作成日が一致する課題は登録されません。|

## 注意事項

### マッピングファイルの編集
MacOSにおいて、`テキストエディット`アプリでマッピングファイルを編集するとダブルクオーテーションが変換されてしまいます。
「環境設定」→「スマート引用符」のチェックを外してください。

### CybozuLiveのユーザー名
ユーザー名の前後に空白が含まれていると移行できません。

## ライセンス

MIT License

* http://www.opensource.org/licenses/mit-license.php

## お問い合わせ

お問い合わせは下記サイトからご連絡ください。

https://backlog.com/ja/contact/

[Backlog]: https://backlog.com/ja/



# Backlog Migration for CybozuLive

Migrate your projects from CybozuLive to [Backlog].

* Backlog
    * [https://backlog.com](https://backlog.com/)
    
## Requirements
* **Java 8**
* The Backlog Space's **administrator** roles.


Download
------------

Please download the jar file from this link, and run from the command line as follows.

https://github.com/nulab/BacklogMigration-CybozuLive/releases

    java -jar backlog-migration-cybozulive-[latest version].jar

To use via proxy server, run from the command line as follows.

    java -Djdk.http.auth.tunneling.disabledSchemes= -Dhttps.proxyHost=[proxy host name] -Dhttps.proxyPort=[proxy port] -Dhttps.proxyUser=[proxy user] -Dhttps.proxyPassword=[proxy password] -jar backlog-migration-cybozulive-[latest version].jar
            
## How to use
### Preparation

Create a working directory.

    $ mkdir work
    $ cd work
    
Download jar file.
    
Create a data directory.

    $ mkdir backlog-migration
   
### Export CSV file from CybozuLive
1. Open the appropriate group and click [Settings] > [Export]
2. Confirm that the 形式 is `標準` and the 文字コード is `UTF-8` and click [Download]
 
Put exported files into `backlog-migration` directory.
    
    
### Init command
Execute the [** init **] command to initialize the application and collect CSV data. 
After that it will create a mapping file.
(The mapping file is used to link data between Backlog and CybozuLive.)

    java -jar backlog-migration-cybozulive-[latest version].jar \
      init \
      --backlog.key   [Backlog of API key] \
      --backlog.url   [URL of Backlog] \
      --projectKey    [Backlog project key]

Sample commands:

    java -jar backlog-migration-cybozulive-[latest version].jar \
      init \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxxxx.backlog.jp \
      --projectKey BACKLOG_PROJECT

The mapping files are created as follows.

- backlog-migration/mappings/users.json (user)
- backlog-migration/mappings/priorities.json (priorities)
- backlog-migration/mappings/statuses.json (status)
      
### Fix the mapping file
A file in CSV format will be automatically created.
The right side is Backlog item. 
For the assignable items, please refer to the following file by reference.

- backlog-migration/mappings/users_list.json (user)
- backlog-migration/mappings/priorities_list.json (priority)
- backlog-migration/mappings/statuses_list.json (status)

### Import command

Run the [**import**] command to import data.

    java -jar backlog-migration-cybozulive-[latest version].jar \
      import \
      --backlog.key   [API key of Backlog] \
      --backlog.url   [URL of Backlog] \
      --projectKey    [Backlog project key]
    
Sample commands:

    java -jar backlog-migration-cybozulive-[latest version].jar \
      import \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxxxx.backlog.jp \
      --projectKey BACKLOG_PROJECT
      
## Limitation

### Backlog's user roles
This program is for the users with the Space's **administrator** roles.

### About migrating chat data
* All issues that are created when migrating chats will have state **Open**.
* The creator of the issue and the creation date are the user who posted in the chat room and the posting date.

### About limitations in CybozuLive
- Forum/ToDo-list
  - Comments：10,000 from the latest
- Event
  - Comments：10,000 from the latest
- Chat
  - Comments：10,000 from the latest
- Can not migrate bulletin forum and event attachments
- The ToDo category can not be migrated.

### About limitations in the Backlog
- Empty comments are not registered.

## Re-importing

When the project key in the Backlog and CybozuLive matches, they will be considered as the same project and data will be imported as follows.

**If the person migrating data is not in the project.**

The project will not be imported and the following message will be shown.  Join the project to migrate data.
To migrate this project, you have to join. Join the project to add issues.

| Item | Specifications |
|:-----------|------------|
| Project | The project will not be added when there is a project with the same project key.  The issues and wikis will be added to the existing project. |
| Issues | Issues with matching subject, creator, creation date are not registered. |

## Important points

### Edit mapping file
In MacOS, when you edit the mapping file with `TextEdit` application, double quotes will be converted.
Please uncheck "Preferences" → "Smart quotes".

### CybozuLive user name
Can not migrate if the user name contains spaces before and after it.

## License

MIT License

* http://www.opensource.org/licenses/mit-license.php

## Inquiry

Please contact us if you encounter any problems during the CybozuLive to the Backlog migration.

https://backlog.com/contact/
