# VASO File Upload

This is Virginia Science Olympiad's facility for collecting files from competitors
during tournaments.  It runs as a service on a virtual machine in an Amazon Web
Services (AWS) Elastic Compute Cloud (EC2).  When running, it presents a simple Web
interface that competitors use to enter their identifying information (team number,
school name, and so on) and upload their files.  The files are stored in an AWS
Simple Storage Service (S3) bucket for retrieval by event supervisors.

## Deploying the Service in EC2

Create a bucket.  This is currently called "virginia-science-olympiad" in the "us-east-1" (Northern Virginia) region, though the bucket name and region are configurable in `application.yml`.

Create two IAM groups, "VasoFileUploaders" and "VasoFileUploadReaders".  Attach the policy "AdministratorAccess" to the first and attach "AmazonS3ReadOnlyAccess" to the second.

Create one IAM profile in the VasoFileUploaders group and install it in your `~/.aws` directory.  The service will use this profile to write to the bucket.  The name of the profile is configurable in `application.yml`.  _(TODO: Figure out the minimum permissions needed for this profile.)_

Create one IAM profile in VasoFileUploadReaders for each person who needs to retrieve files from the bucket.  Typically these people will be event supervisors, and perhaps a score master as well.  Instructions are below for these people to set up Cyberduck to retrieve their files.

Create and start an EC2 instance:

- Amazon Linux 2 AMI, either ARM or x86
- `a1.xlarge` for tournament day, `t2.micro` for free tier.
- EBS storage: 8 or 10 GB
- In the Security Group configuration, it should have SSH (port 22) access by default.  Add HTTP (port 80) and HTTPS (port 443) as well.
- Accept defaults everywhere else
- When you finish the process, you will be given the option to create a new key pair.  If you have a key pair already, go ahead and select it.  Otherwise, give your new key pair a name, download it and save the `.pem` file in your `~/.ssh` directory with `u=rw,go=` permissions.

To connect: `ssh -i ~/.ssh/<key-pair-file-name>.pem <ec2-public-dns-name>`

To transfer files: `sftp -i ~/.ssh/<key-pair-file-name>.pem <ec2-public-dns-name>`

Connect to the instance via SSH and set up some basic user configuration:

- Set up `~/.bashrc` and `~/.vimrc` according to preferences
- Create the `~/.aws` directory and set the permissions on it to `u=rwx,go=`
- Create `~/.aws/config` with `u=rw,go=` permissions:

>> ```
>> [profile <iam-profile-name>]
>> region = us-east-1
>> ```

- Create `~/.aws/credentials` with `u=rw,go=` permissions:

>> ```
>> [<iam-profile-name>]
>> aws_access_key_id = <from-iam-profile>
>> aws_secret_access_key = <from-iam-profile>
>> ```

Install software packages:

- sudo yum update
- sudo yum install java-11-amazon-corretto-headless
- sudo yum install httpd
- sudo yum install mod_ssl

Acquire a web site certificate.  In this case I used the host name `file-upload.virginiaso.org`.  (One handy certificate issuer is https://sslforfree.com/.)  Upload the certificate package to the EC2 instance.  The `~/.ssh` directory is a handy place to store it.

Create the `/etc/certs` directory and unzip the certificate package there.  You should end up with three files, named something like `ca_bundle.crt`, `certificate.crt`, and `private.key`.  Run these commands:

```
sudo chmod u=rw,go= /etc/certs/*
sudo chown root.root /etc/certs/*
```

Upload the file `redirect_http.conf` from the git repository to `/etc/httpd/conf.d`.  (You will likely need to upload to your home directory and then move the file using `sudo`.)  Make sure that file contains correct paths to the three certificate files.

Restart the web server to pick up the new SSL configuration with this command:

```
sudo systemctl restart httpd
```

Build the file upload service jar with the command `./gradlew clean build`.  Issue this command in your git working directory, not on the EC2 instance.  Then upload the jar from the `build/lib` directory to the EC2 instance, in your home directory.  Start the file upload service with this command:

```
java -jar VasoFileUpload-1.1.0-SNAPSHOT.jar &
```

Shut down the service like so:

```
ps -ef | grep VasoFileUpload | grep -v grep
kill <first-number-from-output-of-previous-command>
```

The final piece of configuration is to direct the host name `file-upload.virginiaso.org` to the new server.  To do this, log into the VASO iPage account, click on the `virginiaso.org` domain, click on "DNS & Nameservers" in the left-hand menu, and click on "DNS RECORDS."  Add a DNS record with the following parameters:

- Name: file-upload
- Type: A
- IP Address: Enter the EC2 instance's public IP address
- TTL: 1 hour
- Priority: Leave blank

Once that DNS record takes effect, either `http://file-upload.virginiaso.org/` or `https://file-upload.virginiaso.org/` should bring up the file upload service's index page.

Deployment references:

- https://simpleprogrammingguides.com/deploy-a-spring-boot-application-to-aws-cloud/
- https://medium.com/javarevisited/spring-boot-aws-8cf2f1df84a6
- https://aws.amazon.com/premiumsupport/knowledge-center/acm-ssl-certificate-ec2-elb/



## Retrieving the Files

The file upload server stores the files in Amazon Web Services (AWS) Simple Storage
Service (S3).  In the vernacular, this is "the cloud," or at least Amazon's cloud.
There are two ways to get the files.

### Method 1: Cyberduck (Recommended)

Cyberduck is free software for managing files over several kinds of remote
connections, which you can download from here:

```
https://cyberduck.io/download/
```

Install it, start it up, and use the Open Connection command.  (This is under the
File menu on the Mac -- not sure where it is on Windows.)  Change the connection
type to Amazon S3 and leave the server and port set to their default values of
`s3.amazonaws.com` and `443`.  Enter your access key ID and secret access key, and
press Connect.  At this point you should see a folder hierarchy starting at
`virginia-science-olympiad` and the rest should be pretty obvious, similar to a
Finder window (on Mac) or File Explorer (on Windows).

### Method 2: AWS S3 Console

Open this link:

```
https://363197948456.signin.aws.amazon.com/console
```

Enter your user name (see below) and password (coming in the next email).  It will
ask you to change your password, so do that, and then you can navigate the folder
hierarchy.  This method is nice in that you don't need to install any software,
but it's clunkier, and you can only download files one at a time.

### The File Layout

The files are arranged as follows:

```
virginia-science-olympiad
   vaso-file-upload
      <event-name>-<division>
         List of files
         <event-name>-<division>-submissions.csv
```

Each file name has been constructed from the file name given by the competitors
and the identifying information they entered in the upload form.  For example, if
team B47 is the 17<sup>th</sup> team to upload their file in Vehicle Design, and their file
was called `design.pdf`, then the file in S3 will be located in the folder
`vehicleDesign-B` and it will be called `B47-design-017a.pdf`.  The team number is
prepended to the file name, and the upload ordinal is appended afterwards.  The
"a" after the ordinal indicates that it's the first file on the form.  Vehicle
Design and WICI only ask for one file, so they will always be "a".  For
Helicopter, the "a" file is the flight log and the "b" file is the video.  For
Detector Design, the "a" file is the design log and the "b" file is the program
code.

Each event folder also contains one special file called
`<event-name>-<division>-submissions.csv`.  This is a CSV (spreadsheet file) that
contains the form entries.  Each upload is one row in the file, with the
following column headings:

- EVENT: The event name
- ID: The ordinal number appended to the file name
- VA_DATE_TIME: The date and time of the upload, in Eastern standard time
- DIVISION
- TEAM_NUMBER
- SCHOOL_NAME
- TEAM_NAME
- STUDENT_NAMES
- NOTES: A free text field for the students to use to communicate unusual
  circumstances
- HELICOPTER_MODE: Used only in Helicopter
- FLIGHT_DURATION: Used only in Helicopter
- PASS_CODE: Used only in Helicopter
- UTC_TIME_STAMP: The same value as VA_DATE_TIME, except in UTC.  This is
  for the benefit of the server program -- you can safely ignore it.
- FILE_NAME_0: The first file name
- FILE_NAME_1: The second file name, if there is one
- FILE_NAME_2 through FILE_NAME_9: Unused

There are a few differences for the Helicopter event.  For most events, the
students visit the upload form only once, at the end of the event.  But for
Helicopter, they visit twice, once at the beginning to get their unique word,
and again at the end to upload their two files.  In S3, what you will see is
two folders called helicopterStart and helicopterFinish.  The first will
contain only a CSV file, in which you will find team identifying information,
their start time, and their unique word (in the PASS_CODE column).  The second
folder will contain a CSV file and the uploaded flight logs and videos.  In
the second CSV, the PASS_CODE column will be blank, so you will need to
correlate the two files in order to verify their unique word and check their
upload time.

The HELICOPTER_MODE column will be set to one of the following values in the
second CSV:

- TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS
- TWO_HELICOPTERS_ONE_STUDENT_TWO_VIDEOS
- ONE_HELICOPTER_ONE_STUDENT_TWO_VIDEOS
