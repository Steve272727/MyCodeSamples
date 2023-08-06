#!/bin/bash

#A sample bash script using OpenSSL to decrypt an AES256/CBC key that has been encrypted using RSA/OAEPSHA256
#The RSA used an X509 certificate public key. This script will use the matching secrete private RSA key for the RSA decryption.
#The script will check the RSA key matches the X509 certificate before decrypting
#The decrypted AES256 bit key is converted from binary to hexencoding then the IV and AES KEY extracted
#The script will loop through all encrypted files named *.xml.bin, decrtypt each to *.xml then delete the encrypted file
#Note: tested in Kali Linux with OpenSSL 3.0.8 7 Feb 2023 (Library: OpenSSL 3.0.8 7 Feb 2023)

#Script folders:
#./key - the X509 certificate and matching private key are placed in here
#./input - encrypted files with encrypted AES key are placed in here for decryption

certificate=./key/Certificate.crt
rsaprivatekey=./key/Private.key
#rsaprivatekey=./key/Wrong.key

#folder location of encrypted files
inputFolder="./input"

#check the certificate and private key match
echo "Checking the X509 certificate $certificate matches the RSA private key $rsaprivatekey"

certModulus=$(openssl x509 -noout -modulus -in $certificate | openssl md5 | awk '{print $2}')
privateKeyModulus=$(openssl rsa -noout -modulus -in $rsaprivatekey | openssl md5 | awk '{print $2}')

if [ "$certModulus" != $"$privateKeyModulus" ]; then
	echo "ERROR: The certificate with modulus $certModulus doesn't match private key with modulus $privateKeyModulus"
	exit 1
else
	echo "SUCCESS: Certificate and private key match!"
fi

#decrypt the AES key using RSA private key
echo Decrypting AES export key using RSA private key
encryptedKey="$inputFolder/ExportKey.bin"
decryptedKey="$inputFolder/ExportKeyDecrypted.bin"
rm -f $decryptedKey
#RSA decryption of AES export key
openssl pkeyutl -decrypt -inkey $rsaprivatekey -keyform PEM -in $encryptedKey -out $decryptedKey -pkeyopt rsa_padding_mode:oaep -pkeyopt rsa_oaep_md:sha256

#Check AES key was decrypted successfully
if [ ! -e "$decryptedKey" ]; then
	echo "ERROR: decrypted AES key not found"
	exit 1
else
	echo "SUCCESS: AES key was decrypted to binary file $decryptedKey"
fi

#Convert binary decrypted key to HEX encoded text file
decryptedKeyHEX="$inputFolder/ExportKeyDecryptedHEX.txt"
echo Converting binary file $decryptedKey to HEX encoded file $decryptedKeyHEX
hexdump -e '1/1 "%.2x"' $decryptedKey > $decryptedKeyHEX 

#Extract AES IV from first 32 characters and AES key from remaining characters
echo Extracing AES IV and Key from $decryptedKeyHEX
iv=$(head -c 32 $decryptedKeyHEX)
aes_key=$(tail -c +33 $decryptedKeyHEX)

#Can output these for debugging
#echo AES IV=$iv
#echo AES KEY=$aes_key

#Decrypt all .xml.bin files in the input folder using AES IV and KEY
echo Decrypting encrypted XML files *.xml.bin in folder $inputFolder

for encryptedFile in $inputFolder/*.xml.bin; do

	#decrypted file, strip the .bin extension
	decryptedFile="${encryptedFile%.xml.bin}.xml"

	#AES decryption
	openssl enc -d -aes-256-cbc -in $encryptedFile -out $decryptedFile -K $aes_key -iv $iv
	echo Decrypted $encryptedFile to $decryptedFile

	#Delete encrypted file
	#echo Deleting $encryptedFile
	rm -f $encryptedFile
done

#Cleaup
echo Cleaning up
rm -f $encryptedKey
rm -f $decryptedKey
rm -f $decryptedKeyHEX

echo Finishing decrypting files

