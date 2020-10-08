#!/bin/bash
version=`cat VERSION`
echo "$version"
version_plus=$(($version + 1))
echo "$version_plus"
cat $version_plus > ./VERSION
../../deploy.sh clearing-house-chaincode $version_plus upgrade Java