import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.*;

public class Tweet extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTextArea taTweetText;
	private JButton btnTweet;
	private JButton btnCancel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Tweet dialogTweet = new Tweet(null);

			Post post = dialogTweet.post;

			if (post != null) {
				System.out.println("Tweet successful!");
			} else {
				System.out.println("Tweet canceled.");
			}

			dialogTweet.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialogTweet.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Tweet(JFrame parent) {
		super(parent);
		setTitle("Tweet");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(null);
		contentPanel.setBounds(12, 10, 462, 450);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		setModal(true);
		contentPanel.setLayout(null);

		{
			taTweetText = new JTextArea();
			taTweetText.setLineWrap(true);
			taTweetText.setWrapStyleWord(true);
			taTweetText.setBounds(56, 102, 338, 158);
			contentPanel.add(taTweetText);
		}

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(10, 470, 464, 33);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				JButton btnTweet = new JButton("Tweet");
				btnTweet.setForeground(new Color(255, 255, 255));
				btnTweet.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tweetPost();
					}
				});
				btnTweet.setActionCommand("OK");
				buttonPane.add(btnTweet);
				getRootPane().setDefaultButton(btnTweet);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}

		setSize(500, 550);
		setLocationRelativeTo(parent);
		var parentLocation = parent.getLocationOnScreen();
		setLocation(parentLocation.x + parent.getWidth() - getWidth(),
				parentLocation.y + parent.getHeight() / 2 - getHeight() / 2 + 50);
		setVisible(true);
	}

	/**
	 * Write tweet.
	 */
	User user = null;
	public Post post;

	private void tweetPost() {
		int post_id = 0; // pst_id
		post_id++;

		final String DB_URL = "jdbc:mysql://localhost/twitter_3rd";
		final String USERNAME = "root";
		final String PASSWORD = "3ksmooN!";

		try {
			Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			// connected to database successfully...

			Statement stmt = conn.createStatement();
			ResultSet rs = null;

			// solution for duplicate entry error!!!
			String s0 = "SELECT DISTINCT MAX(pst_id) from post";
			rs = stmt.executeQuery(s0);
			if (rs.next()) {
				post_id = rs.getInt(1) + 1;
//				System.out.printf("pst_id = %d\n", post_id); // for debugging
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String post_text = taTweetText.getText(); // pst_txt
//		System.out.println(post_text); // for debugging
		String post_image = ""; // pst_img
		String post_video = ""; // pst_vid
		int post_num_of_likes = 0; // pst_nol

		String cur_user_id = Login.cur_user_id;
		String post_user_id = cur_user_id; // pst_usr_id

		if (post_text.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please write something.", "Tweet Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		post = addPostToDatabase(post_id, post_text, post_image, post_video, post_num_of_likes, post_user_id);
		if (post != null) {
			JOptionPane.showMessageDialog(this, "Post ID = " + post_id, "Tweet Success",
					JOptionPane.INFORMATION_MESSAGE);
			dispose();
		} else {
			JOptionPane.showMessageDialog(this, "Try again!", "Tweet Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Add tweet to database.
	 */
	private Post addPostToDatabase(int post_id, String post_text, String post_image, String post_video,
			int post_num_of_likes, String post_user_id) {
		Post post = null;

		final String DB_URL = "jdbc:mysql://localhost/twitter_3rd";
		final String USERNAME = "root";
		final String PASSWORD = "3ksmooN!";

		try {
			Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			// connected to database successfully...

			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO post(pst_id, pst_txt, pst_img, pst_vid, pst_nol, pst_usr_id)"
					+ "VALUES(?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setInt(1, post_id);
			preparedStatement.setString(2, post_text);
			preparedStatement.setString(3, post_image);
			preparedStatement.setString(4, post_video);
			preparedStatement.setInt(5, post_num_of_likes);
			preparedStatement.setString(6, post_user_id);

			// insert row into the post table
			int addedRows = preparedStatement.executeUpdate();
			if (addedRows > 0) {
				post = new Post();
				post.post_id = post_id;
				post.post_text = post_text;
				post.post_image = post_image;
				post.post_video = post_video;
				post.post_num_of_likes = post_num_of_likes;
				post.post_user_id = post_user_id;
			}

			stmt.close();
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return post;
	}
}
