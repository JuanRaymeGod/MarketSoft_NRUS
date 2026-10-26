package marketsoft.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Cifra/descifra archivos con AES-256-GCM, derivando la clave desde una
 * contraseña con PBKDF2 (65 536 iteraciones, SHA-256). El archivo cifrado
 * guarda al inicio la sal y el IV usados, necesarios para derivar la misma
 * clave al descifrar. La contraseña NUNCA se guarda en ningún lado — vive
 * solo en la memoria durante la operación y la decide quien la escribe.
 */
public class CifradorUtil {

    private static final int TAMANO_SAL = 16;
    private static final int TAMANO_IV = 12;
    private static final int ITERACIONES_PBKDF2 = 65536;
    private static final int LONGITUD_LLAVE = 256;
    private static final int LONGITUD_TAG_GCM = 128;

    private CifradorUtil() {
    }

    public static void cifrarArchivo(File origenPlano, File destinoCifrado, char[] contrasena) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] sal = new byte[TAMANO_SAL];
        byte[] iv = new byte[TAMANO_IV];
        random.nextBytes(sal);
        random.nextBytes(iv);

        SecretKeySpec llave = derivarLlave(contrasena, sal);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, llave, new GCMParameterSpec(LONGITUD_TAG_GCM, iv));

        try (FileOutputStream fos = new FileOutputStream(destinoCifrado)) {
            fos.write(sal);
            fos.write(iv);
            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher);
                 FileInputStream fis = new FileInputStream(origenPlano)) {
                fis.transferTo(cos);
            }
        }
    }

    public static void descifrarArchivo(File origenCifrado, File destinoPlano, char[] contrasena) throws Exception {
        try (FileInputStream fis = new FileInputStream(origenCifrado)) {
            byte[] sal = fis.readNBytes(TAMANO_SAL);
            byte[] iv = fis.readNBytes(TAMANO_IV);
            if (sal.length != TAMANO_SAL || iv.length != TAMANO_IV) {
                throw new IOException("Archivo demasiado pequeño para ser un respaldo válido.");
            }
            SecretKeySpec llave = derivarLlave(contrasena, sal);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, llave, new GCMParameterSpec(LONGITUD_TAG_GCM, iv));

            // Si la contraseña es incorrecta o el archivo fue alterado, GCM lanza
            // AEADBadTagException al leer — no se obtiene basura en silencio, falla fuerte.
            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(destinoPlano)) {
                cis.transferTo(fos);
            }
        }
    }

    private static SecretKeySpec derivarLlave(char[] contrasena, byte[] sal) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(contrasena, sal, ITERACIONES_PBKDF2, LONGITUD_LLAVE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] bytesLlave = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(bytesLlave, "AES");
    }
}
