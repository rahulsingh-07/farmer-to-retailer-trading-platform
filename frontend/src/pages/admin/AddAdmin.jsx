import React, { useState } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/AddAdmin.css";

const AddAdmin = () => {
	const { token } = useAuth();
	const [form, setForm] = useState({
		fullName: "",
		email: "",
		phoneNumber: "",
		password: "",
	});
	const [loading, setLoading] = useState(false);

	const updateField = (key, value) => {
		setForm((prev) => ({ ...prev, [key]: value }));
	};

	const validate = () => {
		const name = form.fullName.trim();
		const email = form.email.trim();
		const phone = form.phoneNumber.trim();

		if (!name) return "Full name is required";
		if (name.length < 3) return "Full name must be at least 3 characters";
		if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
			return "Enter a valid email";
		}
		if (!phone) {
			return "Phone number is required";
		}
		if (phone && !/^\+?[0-9\-\s]{7,15}$/.test(phone)) return "Enter a valid phone number";
		return "";
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		const validationError = validate();
		if (validationError) {
			toast.error(validationError);
			return;
		}
		setLoading(true);
		try {
			const res = await api.post(
				"/admin/newAdmin",
				{
					fullName: form.fullName.trim(),
					email: form.email.trim(),
					phoneNumber: form.phoneNumber.trim(),
				},
				token
			);
			toast.success(res?.message || "Admin created successfully");
			setForm({ fullName: "", email: "", phoneNumber: "" });
		} catch (err) {
			toast.error(err.message || "Failed to create admin");
		} finally {
			setLoading(false);
		}
	};

	return (
		<div className="admin-form-container">
			<h2>Create New Admin</h2>
			<form className="admin-form" onSubmit={handleSubmit}>
				<label className="admin-form-field">
					<span>Full Name *</span>
					<input
						type="text"
						value={form.fullName}
						onChange={(e) => updateField("fullName", e.target.value)}
						placeholder="Enter full name"
					/>
				</label>

				<label className="admin-form-field">
					<span>Email *</span>
					<input
						type="email"
						value={form.email}
						onChange={(e) => updateField("email", e.target.value)}
						placeholder="admin@example.com"
					/>
				</label>

				<label className="admin-form-field">
					<span>Phone Number *</span>
					<input
						type="tel"
						value={form.phoneNumber}
						onChange={(e) => updateField("phoneNumber", e.target.value)}
						placeholder="Enter phone number"
					/>
				</label>

				<button className="admin-submit-btn" type="submit" disabled={loading}>
					{loading ? "Creating..." : "Create Admin"}
				</button>
			</form>
		</div>
	);
};

export default AddAdmin;
