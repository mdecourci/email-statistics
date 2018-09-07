# email-statistics
## Ingestion and Simple Analysis of Enron Emails

### Get Enron files from AWS 
* Change region to US-EAST(N.Virginia)
* Select snapshot (snapshot ID snap-d203feb5) - Enron
* Select create volume
* Check volume created on Volumes dashboard -volxxx
* Goto EC2 dashboard and create an EC2 instance
* Attach enron volume to EC2 instance
* SSH to EC2 instance
* Mount volume in unix
  * lsblk - check disk  for "xvdf"
  * Determine whether to create a file system on the volume
  * `sudo file -s /dev/xvdf` 
  * if `/dev/xvda1: Linux rev 1.0 ext4 filesystem data, UUID=1701d228-e1bd-4094-a14c-8c64d6819362 (needs journal recovery) (extents) (large files) (huge files)`
  * then OK - DO NOT create filesystem

  * `sudo mkdir mount_point` - make directory - ie /vol
  * Mount: - `sudo mount device_name mount_point`
 * Check "df" shows mount

### Get code from Git repo 
`git clone git@github.com:mdecourci/email-statistics.git`

### Test
Verify code by checking for "To" email recipients that have the address "enron.com"

Check "enron.com" exists in test data files;

`cd /email-statistics/src/test/resources`

Open a unix shell and run;

`unzip -c edrm-enron-v2_arora-h_xml.zip  | less | grep 'TagName' | grep '#To' | grep '.com' | grep 'enron' | wc -l`


