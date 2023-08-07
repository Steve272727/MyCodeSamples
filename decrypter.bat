echo off

REM A sample bash script using OpenSSL to decrypt an AES256/CBC key that has been encrypted using RSA/OAEPSHA256
REM The RSA encryption used a public key from an X509 certificate. This script will use the matching secret private RSA key for the RSA decryption.
REM The script will check the RSA private key matches the X509 certificate before decrypting
REM The decrypted AES256 bit key is converted from binary to a hexencoded text file so the IV and AES KEY can be extracted in the format needed by OpenSSL
REM The script will loop through all encrypted files named *.xml.bin in an input folder, decrypt each to *.xml then delete the encrypted file
REM Note: Tested in Windows 10 with OpenSSL 3.1.2 downloaded from https://kb.firedaemon.com/support/solutions/articles/4000121705#Download-OpenSSL

REM Script folders:
REM ./key - the X509 certificate and matching private key are placed in here
REM ./input - encrypted files with encrypted AES key are placed in here for decryption
REM ./decrypter.bat

cls

REM Set path to open ssl executable
set open_ssl="C:\Program Files\openssl-3\x64\bin\openssl.exe"
echo OpenSSL path: %open_ssl%

set certificate=.\key\Certificate.crt
set rsaprivatekey=.\key\Private.key
REM set rsaprivatekey=.\key\Wrong.key
set inputFolder=.\input

echo Checking the X509 certificate %certificate% matches the RSA private key %rsaprivatekey%
set certModulusFile=.\certModulus.txt
set keyModulusFile=.\keyModulus.txt

REM output the cert and key modulus to temp files
%open_ssl% x509 -noout -modulus -in %certificate% | %open_ssl% md5 > %certModulusFile%
%open_ssl% rsa -noout -modulus -in %rsaprivatekey% | %open_ssl% md5 > %keyModulusFile%

REM read cert and key modules into script variables
set /p certModulus=<%certModulusFile%
set /p privateKeyModulus=<%keyModulusFile%

IF EXIST %certModulusFile% (
	del /Q %certModulusFile%
)
IF EXIST %keyModulusFile% (
	del /Q %keyModulusFile%
)

REM remove the 12 charaters in "MD5(stdin)= "
set "certModulusVal=%certModulus:~12%"
set "privateKeyModulusVal=%privateKeyModulus:~12%"

if "%certModulusVal%" neq "%privateKeyModulusVal%" (
    echo ERROR: The certificate with modulus %certModulusVal% doesn't match private key with modulus %privateKeyModulusVal%
    goto done
) else (
    echo SUCCESS: Certificate and private key match!
)

REM decrypt the AES key using RSA private key
echo Decrypting AES export key using RSA private key
set encryptedKey=%inputFolder%\ExportKey.bin
set decryptedKey=%inputFolder%\ExportKeyDecrypted.bin

IF EXIST %decryptedKey% (
	del /Q %decryptedKey%
)

echo encryptedKey=%encryptedKey%
echo decryptedKey=%decryptedKey%

%open_ssl% pkeyutl -decrypt -inkey %rsaprivatekey% -keyform PEM -in %encryptedKey% -out %decryptedKey% -pkeyopt rsa_padding_mode:oaep -pkeyopt rsa_oaep_md:sha256

REM Check AES key was decrypted successfully
IF NOT EXIST %decryptedKey% (
	echo ERROR: decrypted AES key not found
    goto done	
) ELSE (
	echo SUCCESS: AES key was decrypted to binary file %decryptedKey%
)

REM Convert binary decrypted key to HEX encoded text file
set decryptedKeyHEX=%inputFolder%\ExportKeyDecryptedHEX.txt
echo Converting binary file %decryptedKey% to HEX encoded file %decryptedKeyHEX%

IF EXIST %decryptedKeyHEX% (
	del /Q %decryptedKeyHEX%
)

REM In Windows, the CertUtil can be used to do hex encode the binary key using the "12 option"
REM In linux, this can be done with the command: hexdump -e '1/1 "%.2x"' $decryptedKey > $decryptedKeyHEX
echo Running CertUtil to hex encode the binary key file
CertUtil -encodehex %decryptedKey% %decryptedKeyHEX% 12

REM Read the HEX encoded text file into a script variable
set "aesIVandKeyFile=%decryptedKeyHEX%"
set /p aes_iv_and_key=<%aesIVandKeyFile%

REM Extract AES IV from first 32 characters and AES key from remaining characters

REM Get the first 32 characters as the AES HEX IV
set "aes_iv=%aes_iv_and_key:~0,32%"
REM Get remainder of the characters as the AES HEX key
set "aes_key=%aes_iv_and_key:~32%"

REM Can output these just for debugging
REM echo Extracted AES IV=%aes_iv%
REM echo Extracted AES Key=%aes_key%

REM Decrypt all .xml.bin files in the input folder using AES IV and KEY

echo Decrypting encrypted XML files *.xml.bin in folder %inputFolder%

setlocal enabledelayedexpansion

for %%F in ("%inputFolder%\*.xml.bin") do (
    set "sourceFile=%%F"
	
	REM %%~dpnF represents the drive, path, and name of the file from %%F but without the extension. This will remove the .bin
    set "destinationFile=%%~dpnF"

	REM Decrypt the file with AES/CBC	
	%open_ssl% enc -d -aes-256-cbc -in "!sourceFile!" -out "!destinationFile!" -K %aes_key% -iv %aes_iv%
	echo Decrypted "!sourceFile!" to "!destinationFile!"
)

REM Clean up files
echo Cleaning up

REM remove encrypted files
del /Q %inputFolder%\*.xml.bin

REM remove encrypted key files
IF EXIST %encryptedKey% (
	del /Q %encryptedKey%
)
IF EXIST %decryptedKey% (
	del /Q %decryptedKey%
)
IF EXIST %decryptedKeyHEX% (
	del /Q %decryptedKeyHEX%
)

:done
echo done

REM pause