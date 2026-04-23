import { S3Client, GetObjectCommand, PutObjectCommand } from "@aws-sdk/client-s3";
import sharp from "sharp";

const s3 = new S3Client({});

const RESIZE_OPTIONS = {
  width: 1280,
  withoutEnlargement: true,
};

const FORMAT_CONFIG = {
  jpeg: { quality: 80 },
  jpg:  { quality: 80 },
  png:  { compressionLevel: 8 },
  webp: { quality: 80 },
  gif:  {},
};

export const handler = async (event) => {
  const record = event.Records[0];
  const bucket = record.s3.bucket.name;
  const srcKey = decodeURIComponent(record.s3.object.key.replace(/\+/g, " "));

  if (!srcKey.startsWith("original/")) {
    console.log(`Skipping non-original key: ${srcKey}`);
    return;
  }

  const destKey = srcKey.replace("original/", "resized/");
  const ext = srcKey.split(".").pop().toLowerCase();
  const formatConfig = FORMAT_CONFIG[ext] ?? { quality: 80 };

  console.log(`Resizing: ${srcKey} → ${destKey}`);

  const { Body, ContentType } = await s3.send(
    new GetObjectCommand({ Bucket: bucket, Key: srcKey })
  );

  const buffer = Buffer.from(await Body.transformToByteArray());

  const image = sharp(buffer).resize(RESIZE_OPTIONS);

  let resized;
  if (ext === "jpg" || ext === "jpeg") {
    resized = await image.jpeg(formatConfig).toBuffer();
  } else if (ext === "png") {
    resized = await image.png(formatConfig).toBuffer();
  } else if (ext === "webp") {
    resized = await image.webp(formatConfig).toBuffer();
  } else if (ext === "gif") {
    resized = await image.gif().toBuffer();
  } else {
    resized = await image.jpeg({ quality: 80 }).toBuffer();
  }

  await s3.send(
    new PutObjectCommand({
      Bucket: bucket,
      Key: destKey,
      Body: resized,
      ContentType: ContentType ?? `image/${ext}`,
      ACL: "public-read",
    })
  );

  console.log(`Done: ${destKey}`);
};
