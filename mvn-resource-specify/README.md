# package resource file according evn param

## stg
mvn clean package -Dmaven.test.skip=true -Denv=stg -DprofileActive=stg -X

## prd
mvn clean package -Dmaven.test.skip=true -Denv=prd -DprofileActive=prd -X