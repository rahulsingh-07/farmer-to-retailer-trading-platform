import React, { useMemo, useState } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/AddCrop.css";

const initialForm = {
  cropName: "",
  category: "",
  variety: "",
  quantity: "",
  unit: "kg",
  pricePerUnit: "",
  location: "",
  harvestDate: "",
  description: "",
};

const AddCrop = () => {
  const { token, user } = useAuth();
  const [formData, setFormData] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  const [images, setImages] = useState([]); // File[]
  const [imagePreviews, setImagePreviews] = useState([]); // string[]

  const MAX_IMAGES = 5;
  const MIN_IMAGES = 1;

  const categories = useMemo(
    () => ["Cereals", "Pulses", "Vegetables", "Fruits", "Oilseeds", "Spices","Others"],
    []
  );

  const units = useMemo(() => ["kg", "quintal", "ton"], []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleImageChange = (e) => {
  const selected = Array.from(e.target.files || []);
  if (!selected.length) return;

  const merged = [...images, ...selected];
  const seen = new Set();
  const unique = [];
  for (const file of merged) {
    const key = `${file.name}-${file.lastModified}-${file.size}`;
    if (seen.has(key)) continue;
    seen.add(key);
    unique.push(file);
    if (unique.length >= MAX_IMAGES) break;
  }

  if (merged.length > MAX_IMAGES) {
    toast.info(`Only ${MAX_IMAGES} images are allowed. Extra files were skipped.`);
  }

  setImages(unique);
  setImagePreviews(unique.map((file) => URL.createObjectURL(file)));
  e.target.value = "";
  setErrors((prev) => ({ ...prev, images: "" }));
  };

  const removeImage = (index) => {
    const nextImages = images.filter((_, i) => i !== index);
    const nextPreviews = imagePreviews.filter((_, i) => i !== index);
    setImages(nextImages);
    setImagePreviews(nextPreviews);
  };

  const validate = () => {
    const nextErrors = {};

    if (!formData.cropName.trim()) nextErrors.cropName = "Crop name is required";
    if (!formData.category) nextErrors.category = "Choose a category";
    if (!formData.quantity || Number(formData.quantity) <= 0) {
      nextErrors.quantity = "Enter a valid quantity";
    }
    if (!formData.pricePerUnit || Number(formData.pricePerUnit) <= 0) {
      nextErrors.pricePerUnit = "Enter a valid price";
    }
    if (!formData.location.trim()) nextErrors.location = "Location is required";
    if (!formData.harvestDate) nextErrors.harvestDate = "Select a harvest date";
    if (images.length < MIN_IMAGES) {
      nextErrors.images = `At least ${MIN_IMAGES} image is required`;
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const getPayload = () => ({
    cropName: formData.cropName.trim(),
    category: formData.category,
    variety: formData.variety.trim(),
    quantity: Number(formData.quantity),
    unit: formData.unit,
    pricePerUnit: Number(formData.pricePerUnit),
    location: formData.location.trim(),
    harvestDate: formData.harvestDate,
    description: formData.description ? formData.description.trim() : "",
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    if (!token) {
      toast.error("You need to be logged in to add crops.");
      return;
    }

    setSubmitting(true);
    setMessage("");

    const payload = getPayload();
    const formDataToSend = new FormData();
    formDataToSend.append(
  "request",
  new Blob([JSON.stringify(payload)], { type: "application/json" })
);
images.forEach((file) => formDataToSend.append("images", file));


    try {
      const response = await api.upload("/farmer/addCrop", formDataToSend, token);
      const successMsg = response?.message || "Crop added successfully";
      toast.success(successMsg);
      setMessage(successMsg);
      setFormData(initialForm);
      setImages([]);
      setImagePreviews([]);
    } catch (err) {
      const errorMsg = err.message || "Unable to add crop. Please try again.";
      setMessage(errorMsg);
      toast.error(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="add-crop-page">
      <div className="add-crop-hero">
        <div>
          <p className="add-crop-kicker">Crop management</p>
          <h1>Add a new crop</h1>
          <p className="add-crop-subtitle">
            Share harvest-ready lots with retailers. Keep pricing transparent and
            timelines accurate to win more bids.
          </p>
          <div className="add-crop-pills">
            <span>{user?.username ? `${user.username}'s farm` : "Your farm"}</span>
            <span>Secured</span>
          </div>
        </div>

        <div className="add-crop-summary">
          <div className="summary-label">Quick recap</div>
          <div className="summary-row">
            <span>Category</span>
            <span>{formData.category || "—"}</span>
          </div>
          <div className="summary-row">
            <span>Quantity</span>
            <span>
              {formData.quantity ? `${formData.quantity} ${formData.unit}` : "—"}
            </span>
          </div>
          <div className="summary-row">
            <span>Price</span>
            <span>
              {formData.pricePerUnit
                ? `₹${Number(formData.pricePerUnit).toLocaleString()}/${formData.unit}`
                : "—"}
            </span>
          </div>
          <div className="summary-note">
            Data is saved securely. Double-check quantity and pricing before
            publishing.
          </div>
        </div>
      </div>

      <div className="add-crop-card">
        {message && (
          <div
            className={`banner ${
              message.toLowerCase().includes("unable") ? "error" : "success"
            }`}
          >
            {message}
          </div>
        )}

        <form className="add-crop-form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="cropName">Crop name *</label>
              <input
                id="cropName"
                name="cropName"
                type="text"
                placeholder="e.g., Basmati Rice"
                value={formData.cropName}
                onChange={handleChange}
                className={errors.cropName ? "error" : ""}
              />
              {errors.cropName && (
                <p className="error-text">{errors.cropName}</p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="category">Category *</label>
              <select
                id="category"
                name="category"
                value={formData.category}
                onChange={handleChange}
                className={errors.category ? "error" : ""}
              >
                <option value="">Select category</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
              {errors.category && (
                <p className="error-text">{errors.category}</p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="variety">Variety</label>
              <input
                id="variety"
                name="variety"
                type="text"
                placeholder="e.g., 1121 Sella"
                value={formData.variety}
                onChange={handleChange}
              />
            </div>

            <div className="form-group">
              <label htmlFor="quantity">Quantity *</label>
              <div className="input-inline">
                <input
                  id="quantity"
                  name="quantity"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="e.g., 50"
                  value={formData.quantity}
                  onChange={handleChange}
                  className={errors.quantity ? "error" : ""}
                />
                <select name="unit" value={formData.unit} onChange={handleChange}>
                  {units.map((unit) => (
                    <option key={unit} value={unit}>
                      {unit}
                    </option>
                  ))}
                </select>
              </div>
              {errors.quantity && (
                <p className="error-text">{errors.quantity}</p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="pricePerUnit">Price per unit (₹) *</label>
              <input
                id="pricePerUnit"
                name="pricePerUnit"
                type="number"
                min="0"
                step="0.01"
                placeholder="e.g., 2200"
                value={formData.pricePerUnit}
                onChange={handleChange}
                className={errors.pricePerUnit ? "error" : ""}
              />
              {errors.pricePerUnit && (
                <p className="error-text">{errors.pricePerUnit}</p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="location">Pickup location *</label>
              <input
                id="location"
                name="location"
                type="text"
                placeholder="Village, District, State"
                value={formData.location}
                onChange={handleChange}
                className={errors.location ? "error" : ""}
              />
              {errors.location && (
                <p className="error-text">{errors.location}</p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="harvestDate">Harvest / availability date *</label>
              <input
                id="harvestDate"
                name="harvestDate"
                type="date"
                value={formData.harvestDate}
                onChange={handleChange}
                className={errors.harvestDate ? "error" : ""}
              />
              {errors.harvestDate && (
                <p className="error-text">{errors.harvestDate}</p>
              )}
            </div>

            <div className="form-group full-width">
              <label htmlFor="description">Lot details</label>
              <textarea
                id="description"
                name="description"
                rows="4"
                placeholder="Moisture %, cleaning/processing done, bag type, delivery terms, etc."
                value={formData.description}
                onChange={handleChange}
              />
            </div>

            <div className="form-group full-width">
              <label htmlFor="add-image-input">Images *</label>
              <p className="image-hint">Add 1–5 clear photos of your lot.</p>

              <div className="image-grid">
                {imagePreviews.map((src, index) => (
                  <div key={src} className="image-tile">
                    <img src={src} alt={`Crop ${index + 1}`} />
                    <button
                      type="button"
                      className="image-remove-btn"
                      onClick={() => removeImage(index)}
                    >
                      ×
                    </button>
                  </div>
                ))}

                {images.length < MAX_IMAGES && (
                  <label className="image-tile add-tile" htmlFor="add-image-input">
                    <span className="plus-icon">+</span>
                    <span className="add-text">Add image</span>
                    <input
                      id="add-image-input"
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={handleImageChange}
                      style={{ display: "none" }}
                    />
                  </label>
                )}
              </div>

              {errors.images && <p className="error-text">{errors.images}</p>}
            </div>
          </div>

          <div className="form-footer">
            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitting ? "Saving..." : "Save crop"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddCrop;
