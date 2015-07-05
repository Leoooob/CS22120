$(document).ready(function() {
	$('#login-trigger').click(function(){
		$('#loginform').slideToggle();
		$(this).toggleClass('active');
	})
});