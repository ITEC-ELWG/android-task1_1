<?php
include "config.inc.php";

$start = isset( $_GET["start"] ) ? intval( $_GET["start"] ) : 0;
$count = isset( $_GET["count"] ) ? intval( $_GET["count"] ) : 0;
$id = isset( $_GET["id"] ) ? intval( $_GET["id"]) : NULL;

//连接数据库
$con=mysqli_connect($mysql_cfg["host"],$mysql_cfg["user"],$mysql_cfg["password"]);
if(mysqli_connect_errno($con)){
    die("Failed to connect to MySQL: " . mysqli_connect_error());
}
//选择数据库
$sql="USE android_db";
if( !mysqli_query($con,$sql) ){
    die("Failed use database: " . mysqli_error($con) );
}

if( is_null($id) ){
    $sql = "SELECT * FROM wdj_apps ORDER BY statWeekly DESC LIMIT $start,$count";
    $result = mysqli_query($con,$sql);

    if( mysqli_num_rows($result)==0 ) die("[]");
    else{
        $apps = array();
        while($row =mysqli_fetch_array($result)){
            $appDetails['id'] = (int)$row['id'];
			$appDetails['updateTime'] = (int)$row['updateTime'];
            //$appDetails['apkDownloadUrl'] = $row['apkDownloadUrl'];
            $appDetails['apkMinSdkVersion'] = (int)$row['apkMinSdkVersion'];
            $appDetails['apkSize'] = $row['apkSize'];
            $appDetails['apkVersionName'] = $row['apkVersionName'];
            //$appDetails['description'] = $row['description'];
            $appDetails['icon'] = $row['icon'];
            $appDetails['installedCountStr'] = $row['installedCountStr'];
            //$appDetails['packageName'] = $row['packageName'];
            $appDetails['statWeeklyStr'] = $row['statWeeklyStr'];
            $appDetails['tag'] = $row['tag'];
            $appDetails['title'] = $row['title'];

            array_push($apps,$appDetails);
        }
        echo json_encode( $apps , JSON_UNESCAPED_UNICODE );
    }
}else{
    $sql = "SELECT * FROM wdj_apps WHERE id = $id";
    $result = mysqli_query($con,$sql);
    if( mysqli_num_rows($result)==0 ) die("{}");
    $row =mysqli_fetch_array($result);
    $appDetails['apkDownloadUrl'] = $row['apkDownloadUrl'];
    $appDetails['description'] = $row['description'];
    $appDetails['packageName'] = $row['packageName'];

    echo json_encode( $appDetails , JSON_UNESCAPED_UNICODE );
}



?>