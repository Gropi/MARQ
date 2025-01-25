#!/bin/bash 

BASEDIR=$(dirname $0)

rm -r ${BASEDIR}/../Deployment/collector
cp -R ${BASEDIR}/collector ${BASEDIR}/../Deployment
rm -r ${BASEDIR}/../Deployment/encapsulation
cp -R ${BASEDIR}/encapsulation ${BASEDIR}/../Deployment

rm -r ${BASEDIR}/../Dummy/collector
cp -R ${BASEDIR}/collector ${BASEDIR}/../Dummy
rm -r ${BASEDIR}/../Dummy/encapsulation
cp -R ${BASEDIR}/encapsulation ${BASEDIR}/../Dummy

rm -r ${BASEDIR}/../../BasecontainerHandler
cp -R ${BASEDIR}/collector ${BASEDIR}/../..
mv ${BASEDIR}/../../collector ${BASEDIR}/../../BasecontainerHandler
chmod -R 775 ${BASEDIR}/../../BasecontainerHandler/Collector
