<?php
include "config.inc.php";

function apiUrl($start,$count,$timestamp){
    return "http://apps.wandoujia.com/api/v1/apps?type=weeklytopapp&max="
    . $count
    . "&start=" . $start
    . "&opt_fields=apks.downloadUrl.url,apks.minSdkVersion,apks.size,apks.versionName,description,icons.px68,"
    . "installedCount,installedCountStr,packageName,stat.weekly,stat.weeklyStr,tags.*,title"
    . "&callback=jQuery183034656861261464655_1405907034866&_="
    . $timestamp ."000";
}

function formatHtmlText($str){
    $result = str_ireplace(array("<br />","<br>","<br/>"),"\n",$str);
    $result = preg_replace('~\R~u', "\n", $result);    // CRLF,CR to LF
    $result = html_entity_decode( $result, ENT_QUOTES , "UTF-8");
    return $result;
}

function formatSqlText($str){
    $result = str_replace(array("\"","'"),array("\\\"","\\'"),$str);
    return $result;
}

$spiderCount=isset( $_GET["count"] ) ? intval( $_GET["count"] ) : 5;

//连接数据库
$con=mysqli_connect($mysql_cfg["host"],$mysql_cfg["user"],$mysql_cfg["password"]);
if(mysqli_connect_errno($con)){
    die("Failed to connect to MySQL: " . mysqli_connect_error());
}
//创建数据库
$sql="CREATE DATABASE IF NOT EXISTS android_db";
if( !mysqli_query($con,$sql) ){
    die("Failed creating database: " . mysqli_error($con));
}
//选择数据库
if( !mysqli_query($con,"USE android_db") ){
    die("Failed use database: " . mysqli_error($con) );
}
//创建表
$sql= "CREATE TABLE IF NOT EXISTS wdj_apps (
        id INT NOT NULL AUTO_INCREMENT PRIMARY  KEY,
        updateTime INT,
        apkDownloadUrl VARCHAR(400),
        apkMinSdkVersion INT,
        apkSize VARCHAR(20),
        apkVersionName VARCHAR(100),
        description VARCHAR(4000),
        icon VARCHAR(200),
        installedCount INT,
        installedCountStr VARCHAR(20),
        packageName VARCHAR(100),
        statWeekly INT,
        statWeeklyStr VARCHAR(20),
        tag VARCHAR(20),
        title VARCHAR(100)
        )";
if( !mysqli_query($con,$sql) ){
    die("Failed creating table: " . mysqli_error($con));
}
//清空表
if( !mysqli_query($con,"TRUNCATE TABLE wdj_apps") ){
    die( "Failed to truncate table: " . mysqli_error($con) );
}

$updateTime = time();
$appIndex = 0;
for($index=0;$index<$spiderCount;$index++){

    $url = apiUrl($index*50,50,$updateTime);
    $getContent = file_get_contents( $url );

    $jsonStart = iconv_strpos($getContent,"(");
    $jsonContent = iconv_substr($getContent,$jsonStart+1,-2);

    $jsonArray = json_decode($jsonContent,true);
    //var_dump($jsonArray);

    foreach ($jsonArray as $appValue) {
        $apkDownloadUrl = $appValue["apks"][0]["downloadUrl"]["url"];
        $apkMinSdkVersion = $appValue["apks"][0]["minSdkVersion"];
        $apkSize = $appValue["apks"][0]["size"];
        $apkVersionName = $appValue["apks"][0]["versionName"];
        $description = $appValue["description"];
        $description = formatHtmlText($description);
        $description = formatSqlText($description);
        //echo $description ."\n\n\n";

        $icon = $appValue["icons"]["px68"];
        $installedCount = $appValue["installedCount"];
        $installedCountStr = $appValue["installedCountStr"];
        $packageName = $appValue["packageName"];
        $statWeekly = $appValue["stat"]["weekly"];
        $statWeeklyStr = $appValue["stat"]["weeklyStr"];
        $tag = $appValue["tags"][0]["tag"];
        $title = $appValue["title"];
        $title = formatSqlText($title);

        $sql = "INSERT INTO wdj_apps VALUES(
            NULL,
            $updateTime,
            '$apkDownloadUrl',
            $apkMinSdkVersion,
            '$apkSize',
            '$apkVersionName',
            '$description',
            '$icon',
            $installedCount,
            '$installedCountStr',
            '$packageName',
            $statWeekly,
            '$statWeeklyStr',
            '$tag',
            '$title'
            )";
        if( !mysqli_query($con,$sql) ){
            die("Failed to insert appIndex $appIndex $title: ". mysqli_error($con));
        }else echo "Insert appIndex: $appIndex $title <br/>\n";
        $appIndex++;
    }
}

mysqli_close($con);

echo "All done: $appIndex apps <br/>\n";

?> 