rm -rf /sa-operational-resource/aws/*
cp /sa-operational-resource/binary-repo/4.0.0/application-binary.war /sa-operational-resource/aws
sleep 25
cd /sa-operational-resource/aws
jar xvf application-binary.war
sleep 25

aws s3 rm s3://webcommander-dev/1.0.0 --recursive
sleep 25

aws s3 cp /sa-operational-resource/aws/wc/4.0.0 s3://webcommander-dev/1.0.0/wc/4.0.0/ --recursive --acl public-read
