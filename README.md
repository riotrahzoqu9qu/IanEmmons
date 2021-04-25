# VASO File Upload

This is Virginia Science Olympiad's facility for collecting files from competitors
during tournaments.  It runs as a service on a virtual machine in an Amazon Web
Services (AWS) Elastic Compute Cloud (EC2).  When running, it presents a simple Web
interface that competitors use to enter their identifying information (team number,
school name, and so on) and upload their files.  The files are stored in an AWS
Simple Storage Service (S3) bucket for retrieval by event supervisors.

## Building the Service

(Yet to be written)

## Deploying the Service in EC2

(Yet to be written)

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

* EVENT: The event name
* ID: The ordinal number appended to the file name
* VA_DATE_TIME: The date and time of the upload, in Eastern standard time
* DIVISION
* TEAM_NUMBER
* SCHOOL_NAME
* TEAM_NAME
* STUDENT_NAMES
* NOTES: A free text field for the students to use to communicate unusual
  circumstances
* HELICOPTER_MODE: Used only in Helicopter
* FLIGHT_DURATION: Used only in Helicopter
* PASS_CODE: Used only in Helicopter
* UTC_TIME_STAMP: The same value as VA_DATE_TIME, except in UTC.  This is
  for the benefit of the server program -- you can safely ignore it.
* FILE_NAME_0: The first file name
* FILE_NAME_1: The second file name, if there is one
* FILE_NAME_2 through FILE_NAME_9: Unused

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

* TWO_HELICOPTERS_TWO_STUDENTS
* TWO_HELICOPTERS_ONE_STUDENT
* ONE_HELICOPTER_ONE_STUDENT_TWO_VIDEOS
* ONE_HELICOPTER_ONE_STUDENT_ONE_VIDEO
