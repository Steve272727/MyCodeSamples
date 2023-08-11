# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import os
import codecs
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding as sym_padding

class FileDecrypter:
    def __init__(self):
        self.aesCipher = Cipher(
            algorithms.AES(b'\x00' * 32),
            modes.CBC(b'\x00' * 16),
            backend=default_backend()
        )

    def load_encrypted_export_key(self, encrypted_file, rsa_private_key):
        with open(encrypted_file, 'rb') as file:
            encrypted_data = file.read()

        with open(rsa_private_key, 'rb') as file:
            private_key_content = file.read()

        private_key = serialization.load_pem_private_key(
            private_key_content,
            password=None,
            backend=default_backend()
        )

        decrypted_data = private_key.decrypt(
            encrypted_data,
            padding.OAEP(
                mgf=padding.MGF1(algorithm=hashes.SHA256()),
                algorithm=hashes.SHA256(),
                label=None
            )
        )

        iv = decrypted_data[:16]
        key = decrypted_data[16:]

        self.aesCipher = Cipher(
            algorithms.AES(key),
            modes.CBC(iv),
            backend=default_backend()
        )

    def decrypt(self, encrypted_file, decrypted_file):
        with open(encrypted_file, 'rb') as fs_encrypted, codecs.open(decrypted_file, "wb") as fs_decrypted:
            decryptor = self.aesCipher.decryptor()
            padder = sym_padding.PKCS7(128).unpadder()
            buffer_size = 1024
            while True:
                chunk = fs_encrypted.read(buffer_size)
                if len(chunk) != buffer_size:
                    print(len(chunk))
                if not chunk:
                    break
                data_to_write = padder.update(decryptor.update(chunk))
                fs_decrypted.write(data_to_write)

            fs_decrypted.write(decryptor.finalize())
            fs_decrypted.write(padder.finalize())


def main():
    #Notes:
    #Must install the cryptography libraries first via:
    #pip install cryptography
    
    print("File decryption Python Sample Code")

    sample_folder = 'C:\\Data\\Code\\decryption\\file-decryption-demo\\samples'
    #under the sample folder, there is a subfolder called cert_and_key containing the RSA PEM private key
    #under the sample folder, there is a subrolder called encrypted_files where the Exortkey.bin and encrypted files are located

    private_key_file = os.path.join(sample_folder, 'cert_and_key', 'Private.key')
    #private_key_file = os.path.join(sample_folder, 'cert_and_key', 'Wrong.key')

    encrypted_key_file = os.path.join(sample_folder, 'encrypted_files', 'Exportkey.bin')

    decrypter = FileDecrypter()

    # Decrypt the AES256 export key using the RSA private key
    decrypter.load_encrypted_export_key(encrypted_key_file, private_key_file)

    # Decrypt an encrypted file
    encrypted_inventory_file = os.path.join(sample_folder, 'encrypted_files', 'File1.xml.bin')
    decrypted_inventory_file = os.path.join(sample_folder, 'encrypted_files', 'File1.xml')
    decrypter.decrypt(encrypted_inventory_file, decrypted_inventory_file)

    print("Inventory File decrypted and saved to", decrypted_inventory_file)

    # Decrypt an encrypted file
    encrypted_case_file = os.path.join(sample_folder, 'encrypted_files', 'File2.xml.bin')
    decrypted_case_file = os.path.join(sample_folder, 'encrypted_files', 'File2.xml')
    decrypter.decrypt(encrypted_case_file, decrypted_case_file)

    print("Case File decrypted and saved to", decrypted_case_file)

if __name__ == "__main__":
    main()