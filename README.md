# Backlog Migration for CybozuLive
CybozuLiveのグループを[Backlog]に移行するためのツールです。
(English document is described after Japanese)

**Backlog Migration for CybozuLiveはベータバージョンです。Backlog上の既存プロジェクトにインポートする場合は、先に新しく別プロジェクトを作成し、こちらにインポートし内容を確認後、正式なプロジェクトにインポートしてください**

* Backlog
    * [https://backlog.com](https://backlog.com/)

## 必須要件
* **Java 8**
* Backlogの **管理者権限**

ダウンロード
------------

こちらのリンクからjarファイルをダウンロードし、以下のようにコマンドラインから実行します。

[URL]

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
2. 形式を`標準`、文字コードが`Shift_JIS`であることを確認し、[ダウンロード]をクリックする
    
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

### CybozuLive側の制限について
- 掲示板/ToDoリスト
  - コメント数：最新から10,000件
- イベント
  - コメント数：最新から10,000件
- 掲示板やイベントの添付ファイルは移行できません
- ToDoのカテゴリは移行できません。

### Backlog側の制限について
* Backlogで登録可能なユーザー数を超えた場合、インポートは中断されます。

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

## 第三者のトラッキングシステム

当アプリケーションでは、利用状況把握のために、サードパーティのサービス(Mixpanel)によって、移行先のURL、移行先のプロジェクトキーなどの情報を収集します。
トラッキングするデータについてはMixpanelのプライバシーポリシーを参照してください。また、お客様のデータがMixpanelで使用されることを望まない場合は、以下に掲げる方法で使用停止（オプトアウト）することができます。

次のようにoptOutオプションを使用することで使用停止（オプトアウト）することができます。

    java -jar backlog-migration-cybozulive-[latest version].jar \
      import \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxxxx.backlog.jp \
      --projectKey BACKLOG_PROJECT
      --optOut

### Mixpanel

[Mixpanelのプライバシーポリシー](https://mixpanel.com/privacy/ "Mixpanelのプライバシーポリシー")

## License

MIT License

* http://www.opensource.org/licenses/mit-license.php

## お問い合わせ

お問い合わせは下記サイトからご連絡ください。

https://backlog.com/ja/contact/

[Backlog]: https://backlog.com/ja/



# Backlog Migration for CybozuLive

Migrate your projects from CybozuLive to [Backlog].
(英語の下に日本文が記載されています)

* Backlog
    * [https://backlog.com](https://backlog.com/)
    
## Requirements
* **Java 8**
* The Backlog Space's **administrator** roles.


Download
------------

Please download the jar file from this link, and run from the command line as follows.

[Latest jar URL]

    java -jar backlog-migration-cybozulive-[latest version].jar

To use via proxy server, run from the command line as follows.

    java -Djdk.http.auth.tunneling.disabledSchemes= -Dhttps.proxyHost=[proxy host name] -Dhttps.proxyPort=[proxy port] -Dhttps.proxyUser=[proxy user] -Dhttps.proxyPassword=[proxy password] -jar backlog-migration-cybozulive-[latest version].jar
            
## How to use
### Preparation

Create a working directory.

    $ mkdir work
    $ cd work
    
Download jar file.

    [link is here]
    
Create a data directory.

    $ mkdir backlog-migration
   
### Export CSV file from CybozuLive
1. Open the appropriate group and click [Settings] > [Export]
2. Confirm that the 形式 is `標準` and the 文字コード is `Shift_JIS` and click [Download]
 
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

### About limitations in CybozuLive
- Forum/ToDo-list
  - Comments：10,000 from the latest
- Event
  - Comments：10,000 from the latest
- Can not migrate bulletin forum and event attachments
- The ToDo category can not be migrated.

### About limitations in Backlog
- Importing users will be terminated if the number of users will exceed the limit in Backlog.

## Re-importing

When the project key in Backlog and CybozuLive matches, they will be considered as the same project and data will be imported as follows.

**If the person migrating data is not in the project.**

The project will not be imported and the following message will be shown.  Join the project to migrate data.
Importing to this project failed.  You are not a member of this project. Join the project to add issues.

| Item | Specifications |
|:-----------|------------|
| Project | The project will not be added when there is a project with same project key.  The issues and wikis will be added to the existing project. |
| Issues | Issues with matching subject, creator, creation date are not registered. |

## Important points

### Edit mapping file
In MacOS, when you edit the mapping file with `TextEdit` application, double quotes will be converted.
Please uncheck "Preferences" → "Smart quotes".

## Third party tracking system

In this application, we collect information such as source URL, destination URL, migration source project key, migration destination project key, by third party service (Mixpanel) in order to grasp usage situation.
Please refer to Mixpanel's privacy policy for data to be tracked. Also, if you do not want your data to be used in Mixpanel, you can suspend (opt out) by the following methods.

If you want to opt out, please use the optOut option.

    java -jar backlog-migration-cybozulive-[latest version].jar \
      import \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://xxxxxxx.backlog.jp \
      --projectKey BACKLOG_PROJECT
      --optOut

### Mixpanel

[Mixpanel's Privacy Policy](https://mixpanel.com/privacy/ "Mixpanel's Privacy Policy")

## License

MIT License

* http://www.opensource.org/licenses/mit-license.php

## Inquiry

Please contact us if you encounter any problems during the CybozuLive to Backlog migration.

https://backlog.com/contact/
