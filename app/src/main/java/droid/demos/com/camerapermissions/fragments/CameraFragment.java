package droid.demos.com.camerapermissions.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import droid.demos.com.camerapermissions.R;

public class CameraFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = CameraFragment.class.getSimpleName();
    private static final String PATH_IMAGE_DIRECTORY = "AppExample/myPhotos";
    private static final int REQUEST_PHOTO = 1000;
    private static final int REQUEST_GALLERY = 1001;

    private TextView txvResult;
    private ImageView imgvResult;
    private Button btnPhoto, btnGallery;

    private String path;
    private File image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        txvResult = (TextView) view.findViewById(R.id.txvResult);
        imgvResult = (ImageView) view.findViewById(R.id.imgvResult);
        btnGallery = (Button) view.findViewById(R.id.btnGallery);
        btnPhoto = (Button) view.findViewById(R.id.btnPhoto);

        btnPhoto.setOnClickListener(this);
        btnGallery.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGallery:
                checkPermissionsGallery();
                break;
            case R.id.btnPhoto:
                checkPermissionsPhoto();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK) {

            switch (requestCode) {
                case REQUEST_PHOTO:
                    setBitmapToImageView();
                    break;
                case REQUEST_GALLERY:
                    if (data != null) {
                        setImageToImageView(data);
                    } else {
                        Toast.makeText(getContext(), "Ocurrio un error al cargar la imagen", Toast.LENGTH_LONG).show();
                    }
                    break;
            }

        } else {
            Toast.makeText(getContext(), "No se obtuvo la imagen", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PHOTO:
                if (permissions.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(getContext(), "Necesitas los permisos necesarios para tomar fotografía", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_GALLERY:
                if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPictureGallery();
                } else {
                    Toast.makeText(getContext(), "Necesitas habilitar los permisos necesarios", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    private void checkPermissionsPhoto() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), "Debes permitir los permisos necesarios", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PHOTO);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PHOTO);
            }

        }
    }

    private void takePhoto() {
        File imageFile = new File(Environment.getExternalStorageDirectory(), PATH_IMAGE_DIRECTORY);
        boolean isCreateImage = imageFile.exists();
        String imageName = "";

        if (isCreateImage == false) {
            isCreateImage = imageFile.mkdirs();
        }

        if (isCreateImage == true) {
            imageName = getNameImage();
        }

        path = Environment.getExternalStorageDirectory() + File.separator + PATH_IMAGE_DIRECTORY + File.separator + imageName;
        Log.e("TAG", "" + path);
        image = new File(path);

        Intent intent = null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authorities = getContext().getPackageName() + ".provider";
            Uri imageUri = FileProvider.getUriForFile(getContext(), authorities, image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
        }

        startActivityForResult(intent, REQUEST_PHOTO);

    }

    private String getNameImage() {
        return (System.currentTimeMillis() / 1000) + ".jpg";
    }

    private void setBitmapToImageView() {
        MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.e("TAG", "" + path);
            }
        });
        //estas lineas funcionan bien para el caso que no se quiera rotar la imagen
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //imgvResult.setImageBitmap(bitmap);

        //detecta el angulo de la imagen y la rota
        int angle = getCameraPhotoOrientation(getContext(), Uri.fromFile(image), path);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        txvResult.setText(path);
        imgvResult.setImageBitmap(rotated);
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private void checkPermissionsGallery() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getPictureGallery();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), "Necesitas habilitar los permisos necesarios", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY);
            }
        }
    }

    private void getPictureGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void setImageToImageView(Intent intent) {
        Uri uri = intent.getData();
        imgvResult.setImageURI(uri);
    }

}
