#!/bin/bash

# Copy JAR files for Docker build
echo "Copying JAR files to service directories..."

cp admin-service/target/admin-service-1.0.0.jar admin-service/
echo "✓ Copied admin-service JAR"

cp blog-service/target/blog-service-1.0.0.jar blog-service/
echo "✓ Copied blog-service JAR"

cp email-service/target/email-service-1.0.0.jar email-service/
echo "✓ Copied email-service JAR"

# Image-service har versionsnummer nu efter pom.xml-ändringen
cp image-service/target/image-service-1.0.0.jar image-service/
echo "✓ Copied image-service JAR"

cp shop-service/target/shop-service-1.0.0.jar shop-service/
echo "✓ Copied shop-service JAR"

echo "All JAR files copied successfully!"
