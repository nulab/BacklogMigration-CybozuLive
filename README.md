# Backlog Migration for CybozuLive

## How to use
### Preparation

Create a working directory.

    $ mkdir work
    $ cd work
    
Download jar file.

    [link is here]
    
Create a data directory.

    $ mkdir backlog-migration
    
Put exported files into data directory.
    
    
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
      --backlog.url https://nulab.backlog.jp \
      --projectKey BACKLOG_PROJECT

The mapping files are created as follows.
- backlog-migration/mappings/users.json (user)
- backlog-migration/mappings/priorities.json (priorities)
- backlog-migration/mappings/statuses.json (status)
      
### Fix the mapping file
A file in json format will be automatically created.
The right side is Backlog item. 
For the assignable items, please refer to the following file by reference.
- backlog-migration/mappings/users_list.json (user)
- backlog-migration/mappings/priorities_list.json (priority)
- backlog-migration/mappings/statuses_list.json (status)

### Import command

Run the [**import**] command to import data.

    java -jar backlog-migration-cybozulive-[latest version].jar \
      init \
      --backlog.key   [Backlog of API key] \
      --backlog.url   [URL of Backlog] \
      --projectKey    [Backlog project key]
    
Sample commands:

    java -jar backlog-migration-cybozulive-[latest version].jar \
      init \
      --backlog.key XXXXXXXXXXXXX \
      --backlog.url https://nulab.backlog.jp \
      --projectKey BACKLOG_PROJECT