<html>
<br>
<form action="" method="POST" enctype="multipart/form-data">
    <input type="file" name="image" />
    <input type="submit" />
</form>

</html>


<?php
// Require the Composer autoloader.
require 'vendor/autoload.php';

use Aws\S3\S3Client;
use Aws\S3\Exception\S3Exception;

// Instantiate an Amazon S3 client.
$s3 = new S3Client([
    'version' => 'latest',
    'region'  => 'ap-southeast-1',
    'credentials' => [
        'key'    => "YOUR_KEY",
        'secret' => "YOUR_SECRET",
    ]
]);

if (isset($_FILES['image'])) {
    $file_name = $_FILES['image']['name'];
    $temp_file_location = $_FILES['image']['tmp_name'];

    // Upload a publicly accessible file. The file size and type are determined by the SDK.
    try {
        $result = $s3->putObject([
            'Bucket' => 'learning-cloudstorage',
            'Key'    => 'client/uploads/absensi/' .  $file_name,
            'ContentType' => 'image/jpeg',
            'SourceFile'   => $temp_file_location,
            'ACL'    => 'public-read'
        ]);

        echo $url = $result->get('ObjectURL');
    } catch (S3Exception $e) {
        echo "There was an error uploading the file. " . $e->getMessage();
    }
}

?>